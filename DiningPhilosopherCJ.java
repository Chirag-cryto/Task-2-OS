package DiningPhilosopherCJ;


import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.io.FileWriter;
import java.io.IOException;

public class DiningPhilosopherCJ {

    static final int N = 4;

    // At most three philosophers may compete for forks at once.
    static Semaphore room = new Semaphore(N - 1, true);

    static Semaphore[] forks = new Semaphore[N];

    static String[] philosopherStates = new String[N];

    static boolean[] forkInUse = new boolean[N];

    static ArrayList<Event> events = new ArrayList<Event>();

    static class Event {
        String philosopher;
        String message;
        String[] states;
        boolean[] forks;
        int waiting;
        int eating;

        Event(String philosopher, String message,
              String[] states, boolean[] forks,
              int waiting, int eating) {
            this.philosopher = philosopher;
            this.message = message;
            this.states = states;
            this.forks = forks;
            this.waiting = waiting;
            this.eating = eating;
        }
    }

    // Records a consistent snapshot of the current simulation.
    static synchronized void logEvent(
            int id, String message) {

        int waiting = 0;
        int eating = 0;

        for (int i = 0; i < N; i++) {
            if (philosopherStates[i].startsWith("WAITING")) {
                waiting++;
            }

            if (philosopherStates[i].equals("EATING")) {
                eating++;
            }
        }

        String[] stateCopy = philosopherStates.clone();
        boolean[] forkCopy = forkInUse.clone();

        Event event = new Event(
                "Philosopher-" + (id + 1),
                message,
                stateCopy,
                forkCopy,
                waiting,
                eating
        );

        events.add(event);

        System.out.println(
                event.philosopher + " : " + message
                + " | Waiting: " + waiting
                + " | Eating: " + eating
        );
    }

    static synchronized void setState(
            int id, String state, String message) {

        philosopherStates[id] = state;
        logEvent(id, message);
    }

    static synchronized void setFork(
            int fork, boolean inUse, int philosopherId) {

        forkInUse[fork] = inUse;

        String status = inUse ? "IN USE" : "AVAILABLE";

        logEvent(
                philosopherId,
                "Fork-" + (fork + 1) + " is " + status
        );
    }

    static class Philosopher extends Thread {
        int id;
        int leftFork;
        int rightFork;

        Philosopher(int id) {
            this.id = id;
            this.leftFork = id;
            this.rightFork = (id + 1) % N;
        }

        public void run() {
            try {
                // Each philosopher performs two eating rounds.
                for (int round = 1; round <= 2; round++) {

                    setState(id, "THINKING",
                            "Thinking - round " + round);

                    Thread.sleep(500);

                    setState(id, "WAITING FOR ROOM",
                            "Waiting to enter the dining room");

                    room.acquire();

                    try {
                        setState(id, "WAITING FOR FORKS",
                                "Trying to acquire both forks");

                        forks[leftFork].acquire();
                        setFork(leftFork, true, id);

                        forks[rightFork].acquire();
                        setFork(rightFork, true, id);

                        setState(id, "EATING",
                                "Entered eating section - round " + round);

                        Thread.sleep(700);

                        setState(id, "FINISHED EATING",
                                "Exited eating section - round " + round);

                        // Update visual state before releasing each semaphore.
                        setFork(rightFork, false, id);
                        forks[rightFork].release();

                        setFork(leftFork, false, id);
                        forks[leftFork].release();

                    } finally {
                        room.release();
                    }
                }

                setState(id, "FINISHED",
                        "Completed both eating rounds");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                setState(id, "INTERRUPTED",
                        "Philosopher was interrupted");
            }
        }
    }

    static String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    static void createHTML() throws IOException {
        FileWriter writer =
                new FileWriter("DiningPhilosophers.html");

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        html.append("<title>Dining Philosophers Simulation</title>");

        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;background:#101827;color:#edf2f7;margin:0;padding:24px}");
        html.append("h1,h2{margin-top:0} .sub{color:#aab7ca}");
        html.append(".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(210px,1fr));gap:14px}");
        html.append(".card,.panel{background:#1b2739;border:1px solid #34445c;border-radius:12px;padding:16px}");
        html.append(".status{font-weight:bold;color:#8dd8ff;margin:12px 0}");
        html.append(".fork{display:inline-block;background:#26364d;border-radius:8px;padding:8px;margin:4px;font-size:13px}");
        html.append(".used{background:#874242}.free{background:#27664f}");
        html.append("button{padding:10px 15px;margin:4px;border:0;border-radius:7px;cursor:pointer;font-weight:bold}");
        html.append(".timeline{display:grid;gap:8px;margin-top:12px}");
        html.append(".event{padding:12px;background:#202e42;border-radius:8px;border-left:4px solid #63758d}");
        html.append(".event.active{border-left-color:#66d9a5;background:#2a3b4d}");
        html.append(".small{font-size:13px;color:#bac7d9}");
        html.append("@media(max-width:500px){body{padding:12px}}");
     // Dining Philosophers circular visualization
        html.append(".dining-table{");
        html.append("position:relative;width:100%;max-width:650px;");
        html.append("height:460px;margin:20px auto;");
        html.append("background:#111e31;border:1px solid #34445c;");
        html.append("border-radius:14px;overflow:hidden;}");

        html.append(".table-center{");
        html.append("position:absolute;left:50%;top:50%;");
        html.append("transform:translate(-50%,-50%);");
        html.append("width:190px;height:190px;border-radius:50%;");
        html.append("background:#26364d;border:8px solid #475569;");
        html.append("display:flex;align-items:center;justify-content:center;");
        html.append("font-size:20px;font-weight:bold;color:#edf2f7;}");

        html.append(".person{");
        html.append("position:absolute;width:90px;height:90px;");
        html.append("border-radius:50%;border:4px solid #64748b;");
        html.append("background:#334155;display:flex;flex-direction:column;");
        html.append("align-items:center;justify-content:center;");
        html.append("color:white;font-weight:bold;z-index:3;");
        html.append("box-sizing:border-box;transition:background .3s,border-color .3s;}");

        html.append(".person span{font-size:12px;margin-top:5px;}");
        html.append(".person.thinking{background:#1d4ed8;border-color:#60a5fa;}");
        html.append(".person.eating{background:#15803d;border-color:#4ade80;}");
        html.append(".person.waiting{background:#6d28d9;border-color:#c084fc;}");
        html.append(".person.finished{background:#475569;border-color:#cbd5e1;}");

        html.append(".p1{left:calc(50% - 45px);top:15px;}");
        html.append(".p2{right:8%;top:calc(50% - 45px);}");
        html.append(".p3{left:calc(50% - 45px);bottom:15px;}");
        html.append(".p4{left:8%;top:calc(50% - 45px);}");

        html.append(".chopstick{");
        html.append("position:absolute;width:75px;height:8px;");
        html.append("background:#cbd5e1;border:1px solid #94a3b8;");
        html.append("border-radius:5px;z-index:2;");
        html.append("transition:background .3s,border-color .3s;");
        html.append("}");

        html.append(".chopstick.used{background:#f59e0b;border-color:#fde68a;}");
        html.append(".chopstick.available{background:#64748b;border-color:#cbd5e1;}");

        html.append(".f1{left:58%;top:27%;transform:rotate(45deg);}");
        html.append(".f2{left:66%;top:64%;transform:rotate(135deg);}");
        html.append(".f3{left:31%;top:69%;transform:rotate(45deg);}");
        html.append(".f4{left:27%;top:28%;transform:rotate(135deg);}");

        html.append(".diagram-legend{");
        html.append("display:flex;flex-wrap:wrap;gap:14px;");
        html.append("justify-content:center;margin:12px 0;color:#cbd5e1;");
        html.append("font-size:13px;}");

        html.append(".legend-dot{display:inline-block;width:12px;height:12px;");
        html.append("border-radius:50%;margin-right:5px;}");
        html.append("</style></head><body>");

        html.append("<h1>Dining Philosophers Problem</h1>");
        html.append("<p class='sub'>Semaphore-based simulation | Exactly 4 philosophers | Generated from Java execution logs</p>");
     // Circular table visualization
        html.append("<h2>Philosopher and Chopstick Visualization</h2>");

        html.append("<div class='dining-table'>");

        // Center table
        html.append("<div class='table-center'>Dining<br>Table</div>");

        // Four philosophers
        html.append("<div class='person thinking p1' id='visualP0'>");
        html.append("P1<span>THINKING</span></div>");

        html.append("<div class='person thinking p2' id='visualP1'>");
        html.append("P2<span>THINKING</span></div>");

        html.append("<div class='person thinking p3' id='visualP2'>");
        html.append("P3<span>THINKING</span></div>");

        html.append("<div class='person thinking p4' id='visualP3'>");
        html.append("P4<span>THINKING</span></div>");

        // Four chopsticks
        html.append("<div class='chopstick available f1' id='visualF0'></div>");
        html.append("<div class='chopstick available f2' id='visualF1'></div>");
        html.append("<div class='chopstick available f3' id='visualF2'></div>");
        html.append("<div class='chopstick available f4' id='visualF3'></div>");

        html.append("</div>");

        // Legend
        html.append("<div class='diagram-legend'>");
        html.append("<span><span class='legend-dot' style='background:#1d4ed8'></span>Thinking</span>");
        html.append("<span><span class='legend-dot' style='background:#15803d'></span>Eating</span>");
        html.append("<span><span class='legend-dot' style='background:#6d28d9'></span>Waiting</span>");
        html.append("<span><span class='legend-dot' style='background:#f59e0b'></span>Chopstick in use</span>");
        html.append("</div>");
        html.append("<div class='grid'>");
        html.append("<div class='card'><div class='small'>ACTIVE EATERS</div><h2 id='eating'>0</h2></div>");
        html.append("<div class='card'><div class='small'>WAITING PHILOSOPHERS</div><h2 id='waiting'>0</h2></div>");
        html.append("<div class='card'><div class='small'>EVENT PROGRESS</div><h2 id='progress'>0 / ");
        html.append(events.size());
        html.append("</h2></div></div>");

        html.append("<h2 style='margin-top:24px'>Philosophers</h2>");
        html.append("<div class='grid'>");

        for (int i = 0; i < N; i++) {
            html.append("<div class='card'><h3>Philosopher-");
            html.append(i + 1);
            html.append("</h3><div class='status' id='p");
            html.append(i);
            html.append("'>Not started</div><div class='small'>Adjacent forks: Fork-");
            html.append(i + 1);
            html.append(" and Fork-");
            html.append((i + 1) % N + 1);
            html.append("</div></div>");
        }

        html.append("</div><h2 style='margin-top:24px'>Fork Status</h2>");
        html.append("<div class='panel' id='forkPanel'>");

        for (int i = 0; i < N; i++) {
            html.append("<span class='fork free' id='f");
            html.append(i);
            html.append("'>Fork-");
            html.append(i + 1);
            html.append(": AVAILABLE</span>");
        }

        html.append("</div>");

        html.append("<h2 style='margin-top:24px'>Playback</h2>");
        html.append("<div class='panel'>");
        html.append("<button onclick='play()'>Play</button>");
        html.append("<button onclick='pause()'>Pause</button>");
        html.append("<button onclick='nextEvent()'>Next Event</button>");
        html.append("<button onclick='resetSim()'>Reset</button>");
        html.append("<p id='currentMessage'>Press Play or Next Event to begin.</p>");
        html.append("</div>");

        html.append("<h2 style='margin-top:24px'>Execution Timeline</h2>");
        html.append("<div class='timeline' id='timeline'>");

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);

            html.append("<div class='event' id='ev");
            html.append(i);
            html.append("' style='display:none'>");

            html.append("<b>Event ");
            html.append(i + 1);
            html.append(" - ");
            html.append(escapeHTML(e.philosopher));
            html.append("</b><p>");
            html.append(escapeHTML(e.message));
            html.append("</p><div class='small'>Waiting: ");
            html.append(e.waiting);
            html.append(" | Eating: ");
            html.append(e.eating);
            html.append("</div></div>");
        }

        html.append("</div>");

        html.append("<script>");
        html.append("let index=0;let timer=null;const total=");
        html.append(events.size());
        html.append(";");

        html.append("const data=[");

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);

            if (i > 0) html.append(",");

            html.append("{states:[");

            for (int j = 0; j < N; j++) {
                if (j > 0) html.append(",");
                html.append("'").append(e.states[j]).append("'");
            }

            html.append("],forks:[");

            for (int j = 0; j < N; j++) {
                if (j > 0) html.append(",");
                html.append(e.forks[j]);
            }

            html.append("],waiting:").append(e.waiting);
            html.append(",eating:").append(e.eating);
            html.append(",who:'").append(e.philosopher).append("'");
            html.append(",message:'").append(
                    e.message.replace("\\", "\\\\")
                             .replace("'", "\\'")
                             .replace("\r", " ")
                             .replace("\n", " ")
            ).append("'}");
        }

        html.append("];");

        html.append("function showEvent(n){");
        html.append("if(n>=total){pause();return;}");
        html.append("index=n+1;const d=data[n];");
        html.append("document.getElementById('progress').textContent=index+' / '+total;");
        html.append("document.getElementById('waiting').textContent=d.waiting;");
        html.append("document.getElementById('eating').textContent=d.eating;");
        html.append("document.getElementById('currentMessage').textContent=d.who+': '+d.message;");

        html.append("for(let i=0;i<4;i++){");

     // Update existing philosopher status cards
     html.append("document.getElementById('p'+i).textContent=d.states[i];");

     // Update existing fork status cards
     html.append("const f=document.getElementById('f'+i);");
     html.append("f.textContent='Fork-'+(i+1)+': '+(d.forks[i]?'IN USE':'AVAILABLE');");
     html.append("f.className='fork '+(d.forks[i]?'used':'free');");

     // Update philosopher circle in the diagram
     html.append("const person=document.getElementById('visualP'+i);");
     html.append("const state=d.states[i];");
     html.append("let visualState='thinking';");

     html.append("if(state==='EATING') visualState='eating';");
     html.append("else if(state.startsWith('WAITING')) visualState='waiting';");
     html.append("else if(state==='FINISHED'||state==='INTERRUPTED') visualState='finished';");

     html.append("person.className='person '+visualState+' p'+(i+1);");
     html.append("person.innerHTML='P'+(i+1)+'<span>'+state+'</span>';");

     // Update chopstick line
     html.append("const stick=document.getElementById('visualF'+i);");
     html.append("stick.className='chopstick '+(d.forks[i]?'used':'available')+' f'+(i+1);");

     html.append("}");
        html.append("for(let i=0;i<total;i++){");
        html.append("const el=document.getElementById('ev'+i);");
        html.append("el.style.display=i<=n?'block':'none';");
        html.append("el.className=i===n?'event active':'event';");
        html.append("} }");

        html.append("function nextEvent(){pause();if(index<total)showEvent(index);}");
        html.append("function play(){if(timer!==null)return;if(index>=total)resetSim();timer=setInterval(()=>{if(index<total)showEvent(index);else pause();},900);}");
        html.append("function pause(){if(timer!==null){clearInterval(timer);timer=null;}}");
        html.append("function resetSim(){pause();index=0;");
        html.append("document.getElementById('progress').textContent='0 / '+total;");
        html.append("document.getElementById('waiting').textContent='0';");
        html.append("document.getElementById('eating').textContent='0';");
        html.append("document.getElementById('currentMessage').textContent='Press Play or Next Event to begin.';");
        html.append("for(let i=0;i<4;i++){document.getElementById('p'+i).textContent='Not started';");
        html.append("const f=document.getElementById('f'+i);f.textContent='Fork-'+(i+1)+': AVAILABLE';f.className='fork free';}");
        html.append("for(let i=0;i<total;i++)document.getElementById('ev'+i).style.display='none';}");
        html.append("</script></body></html>");

        writer.write(html.toString());
        writer.close();

        System.out.println(
                "\nHTML simulation generated: DiningPhilosophers.html"
        );
    }

    public static void main(String[] args) {
        for (int i = 0; i < N; i++) {
            forks[i] = new Semaphore(1, true);
            philosopherStates[i] = "NOT STARTED";
            forkInUse[i] = false;
        }

        Philosopher[] philosophers = new Philosopher[N];

        for (int i = 0; i < N; i++) {
            philosophers[i] = new Philosopher(i);
        }

        System.out.println("Starting Dining Philosophers Simulation...\n");

        for (int i = 0; i < N; i++) {
            philosophers[i].start();
        }

        for (int i = 0; i < N; i++) {
            try {
                philosophers[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Main thread interrupted.");
                return;
            }
        }

        try {
            createHTML();
        } catch (IOException e) {
            System.out.println("Error creating HTML: " + e.getMessage());
        }
    }
}

