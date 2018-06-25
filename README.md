# Requests
An object oriented request based networking protocol suitable for almost all networking applications

The goal of Requests is to mimick standard function/method calls in an average programming languages. Through this goal, Requests is very easy to integrate with your code.

A java bind of Requests will soon be available with documentation on how to use it. Below is the Requests standard.

# Format of all data transmission in Requests

# `(request/return),requestID,type,name,data`

### `(request/return)`
If the data transmission is a request, then this should be `request`.
If the data transmission is the return value of a request, then this should be `return`.

### `requestID`
This is a unique ID representing a request so it can be identified.

The `requestID` should be a unique, random string created at the time of the request's creation.
Keep it to a reasonable size.

### `type`
The `type` is the "type" of the "function" to be executed, as in what type of value should be returned. Here is a table describing what it can be (more types will be coming soon):
<table>
		<tr><th>Type</th><th>Description</th></tr>
		<tr>
			<td><code>void</code></td>
      <td>A one-way request with no <code>type</code>. There should be no response unless an error is encountered.</td>
		</tr>
  <tr>
			<td><code>string</code></td>
			<td>A request that requires a string response. The data generated from this request should reside in the `data` section when the `return` is called</td>
		</tr>
  <tr>
			<td><code>error</code></td>
			<td>This type signifies that an error was encountered (like an exception occurred) and the error details will be a string in `data`</td>
		</tr>
</table>

### `name`
The `name` is essentially the name of the "function" being called.

### `data`
Used for parameters (if a request) or return value (if a return). This field can be anything, but JSON is recommended.

`data` can have commas in it, so watch out if you are making your own binding (don't just do a `.split(',')`)!
