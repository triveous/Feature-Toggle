package cc.soham.togglesample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.soham.toggle.Toggle;
import cc.soham.toggle.enums.State;
import cc.soham.toggle.objects.Config;
import cc.soham.toggle.objects.Feature;
import cc.soham.toggle.objects.Rule;
import cc.soham.toggle.objects.Value;

/**
 * Sample Config Activity, shows how to
 * - Use {@link Toggle#setConfig(Config)} to configure Toggle
 * - To check for the feature, use {@link Toggle#check(String)} to check for the status of the feature
 *
 */
public class SampleConfigActivity extends AppCompatActivity {
    @Bind(R.id.activity_sample_feature)
    Button featureButton;
    @Bind(R.id.activity_sample_feature_metadata)
    TextView metadataTextView;
    @Bind(R.id.activity_sample_feature_cached)
    TextView cachedTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_base);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Config Sample");
    }

    /**
     * Shows how to configure Toggle by using a {@link Config} object
     */
    @OnClick(R.id.activity_sample_set_config)
    public void setConfigButton_onClick() {
        showMessage("Importing configuration via a Config object");
        Config config = createSampleConfig();
        Toggle.with(SampleConfigActivity.this).setConfig(config);
        showMessage("Importing configuration via a Config object");
    }

    /**
     * Shows how to check for a particular feature, here we check for the 'mixpanel' feature in the config
     * We can pass in additional flags in the {@link Toggle#check(String)} call like default value etc.
     */
    @OnClick(R.id.activity_sample_check)
    public void checkButton_onClick() {
        showMessage("Checking for the feature");
        Toggle.with(SampleConfigActivity.this).check("mixpanel").defaultState(State.ENABLED).start(new cc.soham.toggle.callbacks.Callback() {
            @Override
            public void onStatusChecked(String feature, boolean enabled, String metadata, boolean cached) {
                showMessage("Feature checked");
                updateUiAfterResponse(feature, enabled, metadata, cached);
            }
        });
    }

    /**
     * Update the UI as per the feature state
     * @param feature Name of the feature
     * @param enabled The feature-toggle state of the feature: enabled/disabled
     * @param metadata Metadata attached to the feature
     * @param cached Shows whether this is a cached response or not
     */
    private void updateUiAfterResponse(String feature, boolean enabled, String metadata, boolean cached) {
        featureButton.setText(feature + " is " + (enabled ? "enabled" : "disabled"));
        featureButton.setEnabled(enabled);
        metadataTextView.setText("Metadata: " + metadata);
        cachedTextView.setText("Cached: " + cached);
    }

    /**
     * Simple helper method to show Toasts
     * @param message
     */
    private void showMessage(String message) {
        Toast.makeText(SampleConfigActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Creates a Sample Config object with two sample rules
     * @return
     */
    @NonNull
    private static Config createSampleConfig() {
        List<Rule> rules = new ArrayList<>();
        Value value1 = new Value(14, 23, null, null, null, null, null, null);
        Value value2 = new Value(null, null, null, null, 1453196880000L, null, null, null);

        String metadata = "sample metadata";
        rules.add(new Rule(false, metadata, value1));
        rules.add(new Rule(false, metadata, value2));

        Feature featureVideo = new Feature("video", null, Toggle.ENABLED, rules);
        Feature featureAudio = new Feature("mixpanel", null, Toggle.ENABLED, rules);
        Feature featureSpeech = new Feature("speech", null, Toggle.DISABLED, rules);

        List<Feature> features = new ArrayList<>();
        features.add(featureVideo);
        features.add(featureAudio);
        features.add(featureSpeech);

        return new Config("myapp", features);
    }

}