import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Requests:
 * An object oriented request based networking protocol
 * suitable for almost all networking applications
 */
public class Requests {
    /**
     * An interface used for callbacks for when an `error` or `return` is received
     */
    public interface OnResultCallback {
        /**
         * When a `return` is received, called from inside parseData()
         *
         * @param name the name of the request (the "function" name)
         * @param data the data received in the `return`
         */
        void onReturn(String name, String data);

        /**
         * When an `error` is received, called from inside parseData()
         *
         * @param name  the name of the request (the "function" name)
         * @param error the error received in the `error`
         */
        void onError(String name, String error);
    }

    /**
     * An interface containing callbacks for when a request is received
     */
    public interface OnRequestCallback {
        /**
         * Called (from inside parseData()) when a request is received
         *
         * @param requestID the ID of the request
         * @param name      The name of the request (the "function" name)
         * @param data      The data/parameters associated with the request
         */
        void onRequest(String requestID, String name, String data);
    }

    /**
     * A member that specifies how long the requestID should be
     * 10 is recommended but can be changed to your choosing
     * Smaller numbers reduce the size of requests, but are less random and can cause issues
     */
    private static final int requestIDLength = 10;
    /**
     * How many unfulfilled requests to save in memory
     * 100 is recommended for a client
     * 10-50 is recommended for a server (as more have the possibility to clog up memory)
     */
    private static final int awaitingRequestsLimit = 100;

    /**
     * A member that holds unfulfilled requests in memory so callbacks can be later executed
     */
    private ConcurrentHashMap<String, OnResultCallback> createdRequests = new ConcurrentHashMap<>();
    /**
     * The callback to call when a request is received
     * Called from within parseData()
     */
    private OnRequestCallback onRequestCallback;
    /**
     * A callback that is called when there is a requestID that is not in createdRequests
     * Called from within parseData()
     * This may be called when a request is returned but null was passed to generateRequest()
     */
    private OnResultCallback onUnknownRequestID;

    /**
     * The constructor for Requests that initializes the necessary fields
     *
     * @param onRequestCallback          The callback to call when a request is received (can be null)
     * @param onDataWithUnknownRequestID The callback to call from within parseData() when an
     *                                   unknown requestID is in a received `return` or `error`
     *                                   See the member's javadoc for more information
     */
    public Requests(OnRequestCallback onRequestCallback,
                    OnResultCallback onDataWithUnknownRequestID) {
        this.onRequestCallback = onRequestCallback;
        this.onUnknownRequestID = onDataWithUnknownRequestID;
    }

    /**
     * A simple and efficient method to find the nth index of a comma in a string
     *
     * @param str the string to search
     * @param n   the nth occurrence of a comma to return the index of (starting at 1)
     * @return the index of the nth occurrence of a comma in the string
     */
    private int findNthComma(String str, int n) {
        int counter = 0;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == ',' && ++counter == n) return i;
        }
        return -1;
    }

    /**
     * This method is used to generate a `return` to send after receiving a `request`
     *
     * @param requestID The requestID supplied by the OnRequestCallback
     * @param name      The name provided by the OnRequestCallback
     * @param data      The data to return
     * @return A string that is suitable to send following the Requests protocol
     */
    public String generateReturn(String requestID, String name, String data) {
        return "return," + requestID + "," + name + "," + data;
    }

    /**
     * This method is used to generate an `error` to send after receiving a `request`
     *
     * @param requestID The requestID supplied by the OnRequestCallback
     * @param name      The name provided by the OnRequestCallback
     * @param error     The error encountered while trying to process the request, as a string
     * @return A string that is suitable to send following the Requests protocol
     */
    public String generateError(String requestID, String name, String error) {
        return "error," + requestID + "," + name + "," + error;
    }

    /**
     * This method creates a `request` to send
     * It also loads a callback into memory for when (and if) the request is fulfilled
     *
     * @param name     The name of the "function" the request is calling
     * @param data     The data/parameters of the request
     * @param callback The callback to call when the request is fulfilled (can be null)
     * @return A string that is suitable to send following the Requests protocol
     */
    public String generateRequest(String name, String data, OnResultCallback callback) {
        StringBuilder requestID = new StringBuilder(requestIDLength);
        Random random = new Random();
        for (int i = 0; i < requestIDLength; ++i) {
            requestID.append((char) (random.nextInt(('z' - 'a') + 1) + 'a'));
        }
        if (callback != null && createdRequests.size() <= awaitingRequestsLimit) {
            createdRequests.putIfAbsent(requestID.toString(), callback);
        }
        return "request," + requestID.toString() + "," + name + "," + data;
    }

    /**
     * A method that parses received data and deals with it accordingly
     * Calls any necessary callbacks
     *
     * @param rawData A string of the data received from the other end
     * @return true if the data received was valid (follows Requests protocol), false otherwise
     */
    public boolean parseData(String rawData) {
        int index1 = findNthComma(rawData, 1),
                index2 = findNthComma(rawData, 2),
                index3 = findNthComma(rawData, 3);
        String type, requestID, name, data;
        try {
            type = rawData.substring(0, index1);
            requestID = rawData.substring(index1 + 1, index2);
            name = rawData.substring(index2 + 1, index3);
            data = rawData.substring(index3 + 1);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
        switch (type) {
            case "request":
                if (onRequestCallback != null) onRequestCallback.onRequest(requestID, name, data);
            case "return":
                OnResultCallback returnCallback = createdRequests.remove(requestID);
                if (returnCallback != null) returnCallback.onReturn(name, data);
                else if (onUnknownRequestID != null) onUnknownRequestID.onReturn(name, data);
            case "error":
                OnResultCallback errorCallback = createdRequests.remove(requestID);
                if (errorCallback != null) errorCallback.onError(name, data);
                else if (onUnknownRequestID != null) onUnknownRequestID.onError(name, data);
        }
        return true;
    }
}
