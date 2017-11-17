# A Mobile Toolkit for Extracting Time-of-Day Fluctuations of Alertness

This repository contains the source code for an Android toolkit to assess circadian fluctuations of cognitive performance.

## Assessing Alertness 

To assess alertness, we use three types of tasks:

![The toolkit consists of three tasks to measure alertness and cognitive performance variations across the day: a Psychomotoric Vigilance Task (left), a Go/No-Go task (middle), and a Multiple Object Tracking task (right).](media/task-overview.png "The toolkit consists of three tasks to measure alertness and cognitive performance variations across the day: a Psychomotoric Vigilance Task (left), a Go/No-Go task (middle), and a Multiple Object Tracking task (right).")

- The Psychomotor Vigilance Test (PVT): The PVT measures the reaction time to a visual stimulus. During the task a visual stimulus is presented randomly every 2 to 6 seconds. The dependent measure of the PVT is the reaction time in milliseconds.

- The Go/No-Go Task (GNG): The GNG task falls into the class of choice reaction time paradigms. It uses two or more distinguishable stimuli, each associated with a unique answer option---in our case, a plain green circle, for which the participant needs to perform a speeded touch down gesture (''go'' trial) and a patterned circle, for which this behavior needs to be inhibited (''no-go'' trial). This task measures reaction time, as well as executive functioning. In our implementation, we use between 8 and 12 stimuli, approximately half of which are no-go stimuli, appearing at random intervals of 1 to 8 seconds. If ignored, stimuli are shown for a maximum of 3 seconds. 
Therefore, the GNG task provides reaction time measures in milliseconds on correctly identified targets and the number of decoys that were reacted to due to failed  inhibition, false alarms.

- The Mutliple Object Tracking Task (MOT): The MOT is a strenuous sustained attention task that requires participants to divide their attention across multiple moving objects. In our implementation 8 blue circles are shown. A subset of 4 target circles briefly flashes to indicate the objects to be tracked. Then, all circles start moving in random, but linear directions. After 10 seconds the circles stop and the test person is asked to identify the target circles. This task is repeated 5 times, the performance measure is the percentage of correctly identified targets.

## Toolkit Usage
The source code contains an Android project including all classes and layouts necessary to log users' performance data to a text file in JSON format. The raw measures can then be extracted from local storage and used for further analysis. 
The key classes comprise the three task types (PVT, GNG, and MOT) as well as the notification scheduler and logging service.

By including the source code, application builders can instruct their application to either collect performance measurements with the activities provided by the toolkit or build their own based on the example log structure given. While the PVT solely collects reaction times, the GNG additionally provides false alarm rates as well as the number of missed targets and correct rejections. The MOT provides a percentage of correctly tracked targets representing multitasking capabilities.

More details on the validation of the toolkit can be found in the following journal publication:

Tilman Dingler, Albrecht Schmidt, and Tonja Machulla. 2017. Building Cognition-Aware Systems: A Mobile Toolkit for Extracting Time-of-Day Fluctuations of Cognitive Performance. Proc. ACM Interact. Mob. Wearable Ubiquitous Technol. (IMWUT) 1, 3, Article 47 (September 2017), 15 pages. DOI: [https://doi.org/10.1145/3132025](https://doi.org/10.1145/3132025)

If you use our toolkit in your product or research, we would very much appreciate a reference to the original article. Thanks.

Release under The MIT License (MIT), copyright: Tilman Dingler, 2017