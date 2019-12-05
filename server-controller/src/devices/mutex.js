
class Mutex {
    constructor () {
        this.queue = [];
        this.locked = false;
    }

    lock (tag) {
		console.log(new Date(), tag + ": require mutuex with locked =  " + this.locked);
        return new Promise((resolve, reject) => {
            if (this.locked) {
                this.queue.push([resolve, reject]);
            } else {
                this.locked = true;
                resolve();
            }
        });
    }

    release (tag) {
		console.log(new Date(),  tag + ": release mutex" );
        if (this.queue.length > 0) {
            const [resolve, reject] = this.queue.shift();
            resolve();
        } else {
            this.locked = false;
        }
    }
	
}

module.exports = Mutex;