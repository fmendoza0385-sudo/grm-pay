package co.grmconexion.pay;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/** Dirección del servidor GRM_PAY (editable desde la app, sin recompilar). */
final class Ajustes {
    // Dirección original del aplicativo en la red FTTH (salida por ether7 del router).
    static final String URL_DEFECTO = "http://172.0.20.6/GRM_PAY/login.php";

    private Ajustes() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("grm_pay", Context.MODE_PRIVATE);
    }

    static String url(Context c) {
        return prefs(c).getString("url", URL_DEFECTO);
    }

    static void guardar(Context c, String url) {
        prefs(c).edit().putString("url", url).apply();
    }

    /** Devuelve la URL normalizada (agrega http:// si falta) o null si no es válida. */
    static String normalizar(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://" + s;
        Uri u = Uri.parse(s);
        if (u.getHost() == null || u.getHost().isEmpty()) return null;
        return s;
    }

    static String host(Context c) {
        String h = Uri.parse(url(c)).getHost();
        return h == null ? "" : h;
    }
}
