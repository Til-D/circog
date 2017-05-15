# A Mobile Toolkit for Extracting Time-of-Day Fluctuations of Alertness

This repository contains the source code for an Android toolkit to assess circadian fluctuations of cognitive performance.

## Assessing Alertness 

To assess alertness, we use three types of tasks:
- The Psychomotor Vigilance Test (PVT): The PVT measures the reaction time to a visual stimulus. During the task a visual stimulus is presented randomly every 2 to 6 seconds. The dependent measure of the PVT is the reaction time in milliseconds.
- The Go/No-Go Task (GNG): The GNG task falls into the class of choice reaction time paradigms. It uses two or more distinguishable stimuli, each associated with a unique answer option---in our case, a plain green circle, for which the participant needs to perform a speeded \textit{touch down} gesture (''go'' trial) and a patterned circle, for which this behavior needs to be inhibited (''no-go'' trial). This task measures reaction time, as well as executive functioning. In our implementation, we use between 8 and 12 stimuli, approximately half of which are no-go stimuli, appearing at random intervals of 1 to 8 seconds. If ignored, stimuli are shown for a maximum of 3 seconds. 
Therefore, the GNG task provides reaction time measures in milliseconds on correctly identified targets and the number of decoys that were reacted to due to failed  inhibition, false alarms.
- The Mutliple Object Tracking Task (MOT): The MOT is a strenuous sustained attention task that requires participants to divide their attention across multiple moving objects. In our implementation 8 blue circles are shown. A subset of 4 target circles briefly flashes to indicate the objects to be tracked. Then, all circles start moving in random, but linear directions. After 10 seconds the circles stop and the test person is asked to identify the target circles. This task is repeated 5 times, the performance measure is the percentage of correctly identified targets.

## Usage of the Toolkit


