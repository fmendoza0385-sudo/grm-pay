package co.grmconexion.pay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/** Pantalla principal: muestra GRM_PAY (PHP) en un WebView a pantalla completa. */
public class MainActivity extends Activity {
    private static final int REQ_ARCHIVO = 11, REQ_CONFIG = 12, REQ_PERMISO = 13;

    private WebView web;
    private ProgressBar barra;
    private View error;
    private TextView txtError;
    private ValueCallback<Uri[]> subida;
    private boolean fallo = false;
    private String urlFallida = null;
    private String[] descargaPendiente = null;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        web = findViewById(R.id.web);
        barra = findViewById(R.id.barra);
        error = findViewById(R.id.error);
        txtError = findViewById(R.id.txtError);

        findViewById(R.id.btnReintentar).setOnClickListener(v -> cargar(urlFallida != null ? urlFallida : Ajustes.url(this)));
        findViewById(R.id.btnServidor).setOnClickListener(v ->
                startActivityForResult(new Intent(this, ConfigActivity.class), REQ_CONFIG));

        configurarWeb();
        if (b != null) web.restoreState(b); else web.loadUrl(Ajustes.url(this));
    }

    private void cargar(String url) {
        fallo = false;
        error.setVisibility(View.GONE);
        web.setVisibility(View.VISIBLE);
        web.loadUrl(url);
    }

    private void configurarWeb() {
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        s.setUserAgentString(s.getUserAgentString() + " GRMPayApp/1.0");
        CookieManager.getInstance().setAcceptCookie(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (u.getHost() != null && u.getHost().equals(Ajustes.host(MainActivity.this))) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onPageStarted(WebView v, String url, Bitmap f) {
                fallo = false;
                barra.setProgress(0);
                barra.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                barra.setVisibility(View.GONE);
                if (!fallo) {
                    error.setVisibility(View.GONE);
                    web.setVisibility(View.VISIBLE);
                }
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) {
                    fallo = true;
                    urlFallida = r.getUrl().toString();
                    txtError.setText("No se pudo abrir " + Ajustes.url(MainActivity.this)
                            + "\n\nVerifica que el celular esté conectado a la red WiFi de GRM Conexión y que el servidor esté encendido.");
                    web.setVisibility(View.INVISIBLE);
                    error.setVisibility(View.VISIBLE);
                    barra.setVisibility(View.GONE);
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) { barra.setProgress(p); }

            // La página usa confirm() antes de registrar/anular pagos: se muestra como diálogo nativo
            @Override
            public boolean onJsConfirm(WebView v, String url, String msg, JsResult r) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("Aceptar", (d, w) -> r.confirm())
                        .setNegativeButton("Cancelar", (d, w) -> r.cancel())
                        .setOnCancelListener(d -> r.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onJsAlert(WebView v, String url, String msg, JsResult r) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("Aceptar", (d, w) -> r.confirm())
                        .setOnCancelListener(d -> r.cancel())
                        .show();
                return true;
            }

            // Subir el Excel de facturas (administrador)
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (subida != null) subida.onReceiveValue(null);
                subida = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "Elegir archivo"), REQ_ARCHIVO);
                } catch (Exception e) {
                    subida = null;
                    return false;
                }
                return true;
            }
        });

        // Exportar CSV de pagos
        web.setDownloadListener((url, ua, cd, mime, len) -> descargar(url, ua, cd, mime));
    }

    private void descargar(String url, String ua, String cd, String mime) {
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 28
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            descargaPendiente = new String[]{url, ua, cd, mime};
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISO);
            return;
        }
        try {
            String nombre = URLUtil.guessFileName(url, cd, mime);
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.setMimeType(mime);
            String ck = CookieManager.getInstance().getCookie(url);
            if (ck != null) r.addRequestHeader("Cookie", ck);
            r.addRequestHeader("User-Agent", ua);
            r.setTitle(nombre);
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombre);
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
            Toast.makeText(this, "Descargando " + nombre + "…", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo descargar el archivo.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_PERMISO && descargaPendiente != null) {
            String[] d = descargaPendiente;
            descargaPendiente = null;
            if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) descargar(d[0], d[1], d[2], d[3]);
            else Toast.makeText(this, "Sin permiso para guardar la descarga.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_ARCHIVO && subida != null) {
            subida.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            subida = null;
        } else if (req == REQ_CONFIG && res == RESULT_OK) {
            cargar(Ajustes.url(this));
        }
    }

    @Override
    public void onBackPressed() {
        String u = web.getUrl() == null ? "" : web.getUrl();
        boolean enInicio = u.contains("/dashboard.php") || u.contains("/login.php");
        if (error.getVisibility() == View.VISIBLE || enInicio || !web.canGoBack()) super.onBackPressed();
        else web.goBack();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}
