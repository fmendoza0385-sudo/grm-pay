package co.grmconexion.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private final Handler h = new Handler(Looper.getMainLooper());
    private final Runnable avanzar = () -> {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    };
    private TextView txtServidor;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);
        txtServidor = findViewById(R.id.txtServidor);
        // Toque en el servidor = cambiarlo (pausa el avance automático)
        txtServidor.setOnClickListener(v -> startActivity(new Intent(this, ConfigActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtServidor.setText("Servidor: " + Ajustes.url(this) + "\nToca aquí para cambiarlo");
        h.postDelayed(avanzar, 1600);
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(avanzar);
    }
}
