Readers-Writers Problem – Semaphore Based
Overview:
This project implements the Readers-Writers Problem in Java using threads and semaphores to manage concurrent access to a shared resource.

Concepts Used:
Multithreading: Reader and Writer threads execute concurrently.
Semaphores: mutex protects the reader counter, while resource controls access to the shared resource.
Synchronization: Synchronized blocks protect shared state updates and event logging.
Thread Coordination: start(), sleep(), and join() manage thread execution and simulate reading and writing.
Event Logging: The program records waiting states, reading and writing activities, and resource status.

HTML Visualization: An HTML file is generated from actual execution events, with Play, Pause, Next Event, and Reset controls.

Working Principle:
Multiple readers can read simultaneously, but writers require exclusive access. The first reader locks the shared resource, and the last reader releases it. Writers acquire the resource before writing and release it afterward.



Readers-Writers Problem – Monitor Based
Overview:
This project implements the Readers-Writers Problem in Java using a monitor-based synchronization technique. It uses threads and synchronized methods to control access to a shared resource.

Concepts Used:
Multithreading: Reader and Writer threads execute concurrently.
Monitor: A shared Monitor class manages access to the resource.
Synchronization: The synchronized keyword ensures that only one thread at a time executes the monitor methods.
wait() and notifyAll(): Threads wait when access is not allowed and are notified when the resource becomes available.
Shared Resource Management: Multiple readers can read simultaneously, but only one writer can write at a time, without active readers.
Event Logging: The program records reader and writer states, waiting counts, and resource status.

Working Principle:
Readers can access the shared resource simultaneously when no writer is active or waiting. Writers must wait until all readers finish and the resource is available. The monitor uses wait() to suspend threads and notifyAll() to notify waiting threads when access conditions may have changed.



Dining Philosophers Problem – Semaphore Based
Overview:
This project implements the Dining Philosophers Problem in Java using threads and semaphores. It demonstrates how multiple philosophers share limited resources (forks) while avoiding deadlock.

Concepts Used:
Multithreading: Four philosopher threads execute concurrently.
Semaphores: Each fork has a semaphore to ensure that only one philosopher can use it at a time.
Deadlock Prevention: A room semaphore allows at most three philosophers to compete for forks simultaneously, helping prevent circular-wait deadlock.
Synchronization: Synchronized methods manage philosopher states, fork status, and event logging.
Thread Coordination: start(), sleep(), and join() manage thread execution and simulate thinking and eating.
Event Logging: The program records philosopher activities, fork usage, and the number of waiting and eating philosophers.

Working Principle:
Each philosopher alternates between thinking and eating for two rounds. To eat, a philosopher first acquires permission to enter the dining room and then acquires both adjacent forks. After eating, the philosopher releases both forks and the room permit. Allowing at most three philosophers to compete for forks at once prevents the classic circular-wait deadlock.



Dining Philosophers Problem – Monitor Based
Overview:
This project implements the Dining Philosophers Problem in Java using threads and a monitor-based synchronization technique. It demonstrates how multiple philosophers share chopsticks while coordinating access to shared resources.

Concepts Used:
Multithreading: Four philosopher threads execute concurrently.
Monitor: A shared Monitor class manages philosopher states and controls access to chopsticks.
Synchronization: The synchronized keyword ensures that monitor methods are executed safely by one thread at a time.
wait() and notifyAll(): Philosophers wait when their neighboring philosophers are eating and are notified when the resource conditions may have changed.
Deadlock Avoidance: A philosopher starts eating only when neither neighboring philosopher is eating, preventing adjacent philosophers from eating simultaneously.
Event Logging: The program records philosopher states, chopstick usage, and the number of waiting and eating philosophers.

Working Principle:
Each philosopher alternates between thinking and eating for two rounds. When a philosopher becomes hungry, the monitor checks whether the neighboring philosophers are eating. If neither neighbor is eating, the philosopher can start eating; otherwise, the philosopher waits. After eating, the philosopher returns to the thinking state and calls notifyAll() to notify waiting threads.
