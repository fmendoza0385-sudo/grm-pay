package co.grmconexion.pay;

import android.app.Activity;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigActivity extends Activity {
    private EditText txtUrl;
    private TextView txtMsg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_config);
        txtUrl = findViewById(R.id.txtUrl);
        txtMsg = findViewById(R.id.txtMsg);
        txtUrl.setText(Ajustes.url(this));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
        findViewById(R.id.btnRestablecer).setOnClickListener(v -> txtUrl.setText(Ajustes.URL_DEFECTO));
        txtUrl.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_DONE) { guardar(); return true; }
            return false;
        });
    }

    private void guardar() {
        String u = Ajustes.normalizar(txtUrl.getText().toString());
        if (u == null) {
            txtMsg.setText("Escribe una dirección válida, por ejemplo http://172.0.20.6/GRM_PAY/login.php");
            return;
        }
        Ajustes.guardar(this, u);
        setResult(RESULT_OK);
        finish();
    }
}
