package cc.soham.toggle.network;

import android.os.AsyncTask;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import cc.soham.toggle.ConversionUtils;
import cc.soham.toggle.FeatureCheckRequest;
import cc.soham.toggle.PersistUtils;
import cc.soham.toggle.enums.State;
import cc.soham.toggle.objects.Product;

/**
 * An {@link AsyncTask} that checks the latest config for a given {@link FeatureCheckRequest}
 */
public class CheckLatestAsyncTask extends AsyncTask<Void, Void, FeatureCheckResponse> {
    final FeatureCheckRequest featureCheckRequest;

    public CheckLatestAsyncTask(FeatureCheckRequest featureCheckRequest) {
        this.featureCheckRequest = featureCheckRequest;
    }

    /**
     * Attempt to download the config file and process the feature (wrt the config) here
     * Also return cached responses wherever the network is not available
     * @param params
     * @return
     */
    @Override
    protected FeatureCheckResponse doInBackground(Void... params) {
        return getFeatureCheckResponse(featureCheckRequest);
    }

    /**
     * Initiating the {@link cc.soham.toggle.callbacks.Callback} is on the UI thread, when applicable
     * @param featureCheckResponse the {@link FeatureCheckResponse} after downloading and processing
     */
    @Override
    protected void onPostExecute(final FeatureCheckResponse featureCheckResponse) {
        initiateCallback(featureCheckResponse, featureCheckRequest);
    }

    /**
     * Initate a {@link cc.soham.toggle.callbacks.Callback} if needed
     * @param featureCheckResponse
     * @param featureCheckRequest
     */
    private static void initiateCallback(FeatureCheckResponse featureCheckResponse, FeatureCheckRequest featureCheckRequest) {
        // make the callback if configured
        if (featureCheckRequest.getCallback() != null) {
            if (featureCheckResponse != null) {
                featureCheckRequest.getCallback().onStatusChecked(featureCheckResponse.getFeatureName(), featureCheckResponse.isEnabled(), featureCheckResponse.getMetadata(), false);
            } else {
                featureCheckRequest.getCallback().onStatusChecked(featureCheckRequest.getFeatureName(), featureCheckRequest.getDefaultState() == State.ENABLED, null, true);
            }
        }
    }

    /**
     * Generates a {@link FeatureCheckResponse} for a given {@link FeatureCheckRequest}
     * @param featureCheckRequest the given {@link FeatureCheckRequest} which needs to be downloaded and processed
     * @return the resultant {@link FeatureCheckResponse} after downloading and processing (or cached in case of errors)
     */
    private static FeatureCheckResponse getFeatureCheckResponse(FeatureCheckRequest featureCheckRequest) {
        try {
            // get the url from preferences
            String url = PersistUtils.getSourceUrl(featureCheckRequest.getToggle().getContext());
            // make network request to receive response
            String response = NetworkOperations.downloadUrl(url);
            // convert string to product
            Product product = ConversionUtils.convertStringToProduct(response);
            // store product
            PersistUtils.storeProduct(product);
            // process the resultant product
            FeatureCheckResponse result = featureCheckRequest.getToggle().processProduct(product, featureCheckRequest);
            // disable the cache flag since this is a live request
            result.setCached(false);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureCheckRequest.getToggle().getAndProcessCachedProductSync(featureCheckRequest);
    }

    /**
     * A static helper method to initiate a {@link CheckLatestAsyncTask} call
     * Checks the network for the latest config and then sends the latest response to the callee
     * @param featureCheckRequest
     */
    public static void start(final FeatureCheckRequest featureCheckRequest) {
        if (featureCheckRequest == null) {
            throw new IllegalStateException("Please pass a valid FeatureCheckRequest");
        }
        new CheckLatestAsyncTask(featureCheckRequest).execute();
    }
}
