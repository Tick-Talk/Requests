# Requests
An object oriented request based networking protocol suitable for almost all networking applications

The goal of Requests is to mimick standard function/method calls in an average programming language. Through this goal, Requests is very easy to integrate with your code.

A java bind of Requests is available -- see Requests.java. Below is the Requests standard.

# Format of all data transmission in Requests

## `(request/return/error),requestID,name,data`

### `(request/return/error)`
If the data transmission is:
* a request, then this should be `request`
* the return value of a request, then this should be `return`
* representing a failure encountered while processing a request, then this should be `error`

### `requestID`
This is a unique ID representing a request so it can be identified.

The `requestID` should be a unique, random string (not including any commas) created at the time of the request's creation.
Keep it to a reasonable size.

### `name`
The `name` is essentially the name of the "function" being called. Names are chosen when creating a protocol for an application and must not contain commas.

### `data`
Used for parameters (if a `request`), a return value (if a `return`), or an error (if an `error`). This field can be anything, but JSON is recommended.

`data` can have commas in it, so watch out if you are making your own binding (don't just do a `.split(',')`)!
