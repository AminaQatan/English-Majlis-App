package com.aminaqatan.englishmajlis;

import android.Manifest;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/** English Majlis: the game runs inside a WebView, with the phone's own voice and microphone. */
public class MainActivity extends Activity {
    static final String HOST = "appassets.androidplatform.net";
    WebView web;
    TextToSpeech tts;
    volatile boolean ttsReady = false;
    SpeechRecognizer rec;
    String pendingLang = null;
    final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#083942"));
            getWindow().setNavigationBarColor(Color.parseColor("#083942"));
        }
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0d4a55"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(), "AndroidApp");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (!HOST.equals(u.getHost())) return null;
                String p = u.getPath();
                if (p == null || p.length() == 0 || p.equals("/")) p = "/index.html";
                try {
                    InputStream in = getAssets().open("www" + p);
                    return new WebResourceResponse(mime(p), "utf-8", in);
                } catch (Exception e) {
                    return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
                            new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        setContentView(web);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttsReady = status == TextToSpeech.SUCCESS;
                if (!ttsReady) return;
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { js("window.__androidTTS&&window.__androidTTS('start'," + JSONObject.quote(id) + ")"); }
                    @Override public void onDone(String id) { js("window.__androidTTS&&window.__androidTTS('done'," + JSONObject.quote(id) + ")"); }
                    @Override public void onError(String id) { js("window.__androidTTS&&window.__androidTTS('error'," + JSONObject.quote(id) + ")"); }
                    @Override public void onStop(String id, boolean interrupted) { js("window.__androidTTS&&window.__androidTTS('done'," + JSONObject.quote(id) + ")"); }
                });
            }
        });
        web.loadUrl("https://" + HOST + "/index.html");
    }

    static String mime(String p) {
        String l = p.toLowerCase(Locale.ROOT);
        if (l.endsWith(".html")) return "text/html";
        if (l.endsWith(".js")) return "application/javascript";
        if (l.endsWith(".css")) return "text/css";
        if (l.endsWith(".png")) return "image/png";
        if (l.endsWith(".svg")) return "image/svg+xml";
        if (l.endsWith(".json") || l.endsWith(".webmanifest")) return "application/json";
        return "text/plain";
    }

    void js(final String code) {
        ui.post(new Runnable() {
            @Override public void run() { if (web != null) web.evaluateJavascript(code, null); }
        });
    }

    void sr(String type, String text) {
        js("window.__androidSR&&window.__androidSR(" + JSONObject.quote(type) + "," + JSONObject.quote(text == null ? "" : text) + ")");
    }

    static boolean female(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.contains("female") || n.matches(".*-x-(sfg|iob|iog|tpc|tpf|gba|gbc|gbg|fis|aua|auc|enc|ene)-.*");
    }

    static boolean male(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return (n.contains("male") && !n.contains("female")) || n.matches(".*-x-(iol|iom|tpd|gbb|gbd|rjs|aub|aud|end)-.*");
    }

    List<Voice> voicesFor(String lang, final String gender) {
        List<Voice> out = new ArrayList<Voice>();
        if (!ttsReady || Build.VERSION.SDK_INT < 21) return out;
        try {
            Locale want = Locale.forLanguageTag(lang);
            for (Voice v : tts.getVoices()) {
                Locale l = v.getLocale();
                if (l == null || !"en".equals(l.getLanguage())) continue;
                if (v.getFeatures() != null && v.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)) continue;
                if (!want.getCountry().equalsIgnoreCase(l.getCountry())) continue;
                out.add(v);
            }
            Collections.sort(out, new Comparator<Voice>() {
                int score(Voice v) {
                    int s = v.getQuality();
                    if ("female".equals(gender) ? female(v.getName()) : male(v.getName())) s += 1000;
                    if (v.isNetworkConnectionRequired()) s -= 50;
                    return s;
                }
                @Override public int compare(Voice a, Voice b) { return score(b) - score(a); }
            });
        } catch (Exception e) { }
        return out;
    }

    void startRec(String lang) {
        try {
            if (rec == null) {
                rec = SpeechRecognizer.createSpeechRecognizer(this);
                rec.setRecognitionListener(new RecognitionListener() {
                    @Override public void onReadyForSpeech(Bundle b) { sr("start", ""); }
                    @Override public void onBeginningOfSpeech() { }
                    @Override public void onRmsChanged(float v) { }
                    @Override public void onBufferReceived(byte[] b) { }
                    @Override public void onEndOfSpeech() { }
                    @Override public void onError(int code) { sr("error", String.valueOf(code)); }
                    @Override public void onResults(Bundle b) { sr("final", first(b)); }
                    @Override public void onPartialResults(Bundle b) { sr("partial", first(b)); }
                    @Override public void onEvent(int t, Bundle b) { }
                });
            }
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            rec.startListening(i);
        } catch (Exception e) {
            sr("error", "unavailable");
        }
    }

    static String first(Bundle b) {
        if (b == null) return "";
        ArrayList<String> l = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        return (l == null || l.isEmpty()) ? "" : l.get(0);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (code != 7) return;
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED && pendingLang != null) startRec(pendingLang);
        else sr("error", "denied");
        pendingLang = null;
    }

    @Override
    public void onBackPressed() {
        if (web == null) { super.onBackPressed(); return; }
        web.evaluateJavascript("(window.__androidBack?window.__androidBack():false)", new ValueCallback<String>() {
            @Override public void onReceiveValue(String v) { if (!"true".equals(v)) finish(); }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) tts.stop();
        if (rec != null) rec.cancel();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        if (rec != null) rec.destroy();
        if (web != null) web.destroy();
        super.onDestroy();
    }

    /** Methods the game page can call as window.AndroidApp.* */
    class Bridge {
        @JavascriptInterface
        public boolean isTv() {
            UiModeManager m = (UiModeManager) getSystemService(Context.UI_MODE_SERVICE);
            return (m != null && m.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION)
                    || getPackageManager().hasSystemFeature("android.software.leanback");
        }

        @JavascriptInterface
        public boolean canSpeak() { return ttsReady; }

        @JavascriptInterface
        public boolean canListen() { return SpeechRecognizer.isRecognitionAvailable(MainActivity.this); }

        @JavascriptInterface
        public String voices(String lang, String gender) {
            JSONArray a = new JSONArray();
            for (Voice v : voicesFor(lang, gender)) a.put(v.getName());
            return a.toString();
        }

        @JavascriptInterface
        public void speak(String text, String lang, float rate, String voice, String id) {
            if (!ttsReady) { js("window.__androidTTS&&window.__androidTTS('error'," + JSONObject.quote(id) + ")"); return; }
            try {
                tts.setLanguage(Locale.forLanguageTag(lang));
                if (Build.VERSION.SDK_INT >= 21 && voice != null && voice.length() > 0) {
                    for (Voice v : tts.getVoices()) if (v.getName().equals(voice)) { tts.setVoice(v); break; }
                }
                tts.setSpeechRate(rate);
                Bundle p = new Bundle();
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, p, id);
            } catch (Exception e) {
                js("window.__androidTTS&&window.__androidTTS('error'," + JSONObject.quote(id) + ")");
            }
        }

        @JavascriptInterface
        public void stopSpeak() { if (tts != null) tts.stop(); }

        @JavascriptInterface
        public void listen(final String lang) {
            ui.post(new Runnable() {
                @Override public void run() {
                    if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        pendingLang = lang;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7);
                    } else startRec(lang);
                }
            });
        }

        @JavascriptInterface
        public void stopListen() {
            ui.post(new Runnable() {
                @Override public void run() { if (rec != null) rec.stopListening(); }
            });
        }
    }
}
