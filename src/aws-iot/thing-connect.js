var awsIot = require('aws-iot-device-sdk');
//
// Replace the values of '<YourUniqueClientIdentifier>' and '<YourCustomEndpoint>'
// with a unique client identifier and custom host endpoint provided in AWS IoT cloud
// NOTE: client identifiers must be unique within your AWS account; if a client attempts 
// to connect with a client identifier which is already in use, the existing 
// connection will be terminated.
//
console.log('thingShadows');
var thingShadows = awsIot.thingShadow({
    keyPath: '.\\cert\\broadlink.private.key',
    certPath: '.\\cert\\broadlink.cert.pem',
    caPath: '.\\cert\\root-CA.crt',
    clientId: 'sdk-nodejs-c347ce22-4717-4190-8e89-ef701aa31a8e',
    host: "a3oosh7oql9nlc-ats.iot.us-east-1.amazonaws.com"
});
console.log('thingShadows end');
//
// Client token value returned from thingShadows.update() operation
//
var clientTokenUpdate;

//
// Simulated device values
//
var rval = 190;
var gval = 110;
var bval = 222;

thingShadows.on('connect', function () {

    console.log('connect');
    //
    // After connecting to the AWS IoT platform, register interest in the
    // Thing Shadow named 'RGBLedLamp'.
    //
    thingShadows.register('broadlink', {}, function () {

        // Once registration is complete, update the Thing Shadow named
        // 'RGBLedLamp' with the latest device state and save the clientToken
        // so that we can correlate it with status or timeout events.
        //
        // Thing shadow state
        //
        var rgbLedLampState = {
            "state": {
                "reported": {
                    "red": 200,
                    "blue": 100
                }
            }
        };

        clientTokenUpdate = thingShadows.update('broadlink', rgbLedLampState);
        //
        // The update method returns a clientToken; if non-null, this value will
        // be sent in a 'status' event when the operation completes, allowing you
        // to know whether or not the update was successful.  If the update method
        // returns null, it's because another operation is currently in progress and
        // you'll need to wait until it completes (or times out) before updating the 
        // shadow.
        //
        if (clientTokenUpdate === null) {
            console.log('update shadow failed, operation still in progress');
        }
    });
});
thingShadows.on('status',
    function (thingName, stat, clientToken, stateObject) {
        console.log('received ' + stat + ' on ' + thingName + ': ' +
            JSON.stringify(stateObject));
        //
        // These events report the status of update(), get(), and delete() 
        // calls.  The clientToken value associated with the event will have
        // the same value which was returned in an earlier call to get(),
        // update(), or delete().  Use status events to keep track of the
        // status of shadow operations.
        //
    });

thingShadows.on('delta',
    function (thingName, stateObject) {
        console.log('received delta on ' + thingName + ': ' +
            JSON.stringify(stateObject));
    });

thingShadows.on('timeout',
    function (thingName, clientToken) {
        console.log('received timeout on ' + thingName +
            ' with token: ' + clientToken);
        //
        // In the event that a shadow operation times out, you'll receive
        // one of these events.  The clientToken value associated with the
        // event will have the same value which was returned in an earlier
        // call to get(), update(), or delete().
        //
    });