try {
    var tuners = webapis.tv.channel.getNumOfAvailableTuner();
    console.log("TV has " + tuners + " tuners");
} catch (error) {
    console.log(error.name);
}
