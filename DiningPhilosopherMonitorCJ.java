package DiningphilosopherMonitorCJ;


import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

public class DiningPhilosopherMonitorCJ {

    static final int N = 4;

    static final String THINKING = "THINKING";
    static final String HUNGRY = "HUNGRY";
    static final String EATING = "EATING";
    static final String FINISHED = "FINISHED";

    static Monitor monitor = new Monitor();

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

    	static class Monitor {

        String[] state = new String[N];

        ArrayList<Event> events = new ArrayList<Event>();

        Monitor() {
            for (int i = 0; i < N; i++) {
                state[i] = THINKING;
            }
        }

        int left(int id) {
            return (id + N - 1) % N;
        }

        int right(int id) {
            return (id + 1) % N;
        }

        boolean canEat(int id) {
            return state[left(id)] != EATING
                    && state[right(id)] != EATING;
        }

        synchronized void logEvent(int id, String message) {
            int waiting = 0;
            int eating = 0;

            boolean[] forkStatus = new boolean[N];

            for (int i = 0; i < N; i++) {
                if (state[i] == HUNGRY) {
                    waiting++;
                }

                if (state[i] == EATING) {
                    eating++;
                }
            }

            for (int i = 0; i < N; i++) {
                forkStatus[i] =
                        state[i] == EATING
                        || state[(i + 1) % N] == EATING;
            }

            Event event = new Event(
                    "Philosopher-" + (id + 1),
                    message,
                    state.clone(),
                    forkStatus,
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

        synchronized void think(int id) {
            state[id] = THINKING;
            logEvent(id, "Thinking");
        }

        synchronized void requestToEat(int id)
                throws InterruptedException {

            state[id] = HUNGRY;
            logEvent(id, "Hungry and waiting for chopsticks");

            // Wait until both neighboring philosophers are not eating.
            while (!canEat(id)) {
                wait();
            }

            state[id] = EATING;
            logEvent(id, "Acquired both chopsticks and started eating");
        }

        synchronized void finishEating(int id) {
            state[id] = THINKING;
            logEvent(id, "Finished eating and released chopsticks");

            // Wake up philosophers waiting for their neighbors to finish.
            notifyAll();
        }

        synchronized void finish(int id) {
            state[id] = FINISHED;
            logEvent(id, "Finished all eating rounds");
            notifyAll();
        }
    }

    static class Philosopher extends Thread {
        int id;

        Philosopher(int id) {
            this.id = id;
        }

        public void run() {
            try {
                // Each philosopher eats twice.
                for (int round = 1; round <= 2; round++) {

                    monitor.think(id);
                    Thread.sleep(400);

                    monitor.requestToEat(id);
                    Thread.sleep(700);

                    monitor.finishEating(id);
                    Thread.sleep(300);
                }

                monitor.finish(id);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(
                        "Philosopher-" + (id + 1) + " was interrupted."
                );
            }
        }
    }

    static String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    static String escapeJS(String text) {
        return text.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\r", " ")
                   .replace("\n", " ");
    }

    static void createHTML(ArrayList<Event> events)
            throws IOException {

        FileWriter writer =
                new FileWriter("DiningPhilosophersMonitor.html");

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        html.append("<title>Dining Philosophers - Monitor</title>");

        html.append("<style>");
        html.append("body{font-family:Arial;background:#101827;color:#edf2f7;padding:20px;margin:0}");
        html.append("h1,h2{margin-bottom:12px}.sub{color:#aab7ca}");
        html.append(".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:12px}");
        html.append(".card,.panel{background:#1b2739;border:1px solid #34445c;border-radius:12px;padding:14px}");
        html.append(".dining-table{position:relative;width:100%;max-width:650px;height:440px;margin:20px auto;background:#111e31;border:1px solid #34445c;border-radius:14px;overflow:hidden}");
        html.append(".table-center{position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);width:170px;height:170px;border-radius:50%;background:#26364d;border:8px solid #475569;display:flex;align-items:center;justify-content:center;text-align:center;font-weight:bold;font-size:20px}");
        html.append(".person{position:absolute;width:88px;height:88px;border-radius:50%;border:4px solid #64748b;background:#334155;display:flex;flex-direction:column;align-items:center;justify-content:center;font-weight:bold;z-index:3;box-sizing:border-box;text-align:center;transition:background .25s}");
        html.append(".person span{font-size:10px;margin-top:4px}");
        html.append(".thinking{background:#1d4ed8;border-color:#60a5fa}");
        html.append(".hungry{background:#6d28d9;border-color:#c084fc}");
        html.append(".eating{background:#15803d;border-color:#4ade80}");
        html.append(".finished{background:#475569;border-color:#cbd5e1}");
        html.append(".p1{left:calc(50% - 44px);top:12px}");
        html.append(".p2{right:5%;top:calc(50% - 44px)}");
        html.append(".p3{left:calc(50% - 44px);bottom:12px}");
        html.append(".p4{left:5%;top:calc(50% - 44px)}");
        html.append(".chopstick{position:absolute;width:72px;height:8px;border-radius:5px;background:#64748b;border:1px solid #cbd5e1;z-index:2;transition:background .25s}");
        html.append(".used{background:#f59e0b;border-color:#fde68a}");
        html.append(".f1{left:60%;top:27%;transform:rotate(45deg)}");
        html.append(".f2{left:65%;top:66%;transform:rotate(135deg)}");
        html.append(".f3{left:29%;top:67%;transform:rotate(45deg)}");
        html.append(".f4{left:27%;top:28%;transform:rotate(135deg)}");
        html.append(".legend{display:flex;flex-wrap:wrap;gap:14px;margin:12px 0;color:#cbd5e1;font-size:13px}");
        html.append(".dot{display:inline-block;width:12px;height:12px;border-radius:50%;margin-right:5px}");
        html.append("button{padding:10px 15px;margin:4px;border:0;border-radius:7px;cursor:pointer;font-weight:bold}");
        html.append(".event{background:#202e42;border-left:4px solid #64748b;padding:12px;margin:8px 0;border-radius:7px}");
        html.append(".active{border-left-color:#4ade80;background:#2a3b4d}");
        html.append(".small{font-size:13px;color:#bac7d9}");
        html.append("@media(max-width:500px){body{padding:10px}.dining-table{height:360px}.table-center{width:125px;height:125px}.person{width:72px;height:72px;font-size:12px}.chopstick{width:48px}}");
        html.append("</style></head><body>");

        html.append("<h1>Dining Philosophers Problem</h1>");
        html.append("<p class='sub'>Monitor-based synchronization | Four philosophers | Generated from Java execution events</p>");

        // Circular table and four philosopher circles
        html.append("<h2>Dining Table Visualization</h2>");
        html.append("<div class='dining-table'>");
        html.append("<div class='table-center'>Shared<br>Dining Table</div>");

        for (int i = 0; i < N; i++) {
            String position;

            if (i == 0) position = "p1";
            else if (i == 1) position = "p2";
            else if (i == 2) position = "p3";
            else position = "p4";

            html.append("<div class='person thinking ");
            html.append(position);
            html.append("' id='person");
            html.append(i);
            html.append("'>P");
            html.append(i + 1);
            html.append("<span>THINKING</span></div>");
        }

        // Fork i lies between philosopher i and philosopher i+1.
        for (int i = 0; i < N; i++) {
            html.append("<div class='chopstick f");
            html.append(i + 1);
            html.append("' id='fork");
            html.append(i);
            html.append("'></div>");
        }

        html.append("</div>");

        html.append("<div class='legend'>");
        html.append("<span><i class='dot' style='background:#1d4ed8'></i>Thinking</span>");
        html.append("<span><i class='dot' style='background:#6d28d9'></i>Hungry / Waiting</span>");
        html.append("<span><i class='dot' style='background:#15803d'></i>Eating</span>");
        html.append("<span><i class='dot' style='background:#f59e0b'></i>Chopstick in use</span>");
        html.append("</div>");

        html.append("<div class='grid'>");
        html.append("<div class='card'><div class='small'>EATING</div><h2 id='eating'>0</h2></div>");
        html.append("<div class='card'><div class='small'>WAITING</div><h2 id='waiting'>0</h2></div>");
        html.append("<div class='card'><div class='small'>EVENT PROGRESS</div><h2 id='progress'>0 / ");
        html.append(events.size());
        html.append("</h2></div></div>");

        html.append("<h2>Playback Controls</h2>");
        html.append("<div class='panel'>");
        html.append("<button onclick='play()'>Play</button>");
        html.append("<button onclick='pause()'>Pause</button>");
        html.append("<button onclick='nextEvent()'>Next Event</button>");
        html.append("<button onclick='resetSim()'>Reset</button>");
        html.append("<p id='message'>Press Play or Next Event to start.</p>");
        html.append("</div>");

        html.append("<h2>Execution Timeline</h2>");
        html.append("<div id='timeline'>");

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);

            html.append("<div class='event' id='event");
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

        // Send the actual Java event snapshots to the browser.
        html.append("<script>");
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
            html.append(",who:'").append(escapeJS(e.philosopher)).append("'");
            html.append(",message:'").append(escapeJS(e.message)).append("'}");
        }

        html.append("];");

        html.append("let index=0,timer=null;const total=data.length;");

        html.append("function showEvent(n){if(n>=total){pause();return;}index=n+1;const d=data[n];");
        html.append("document.getElementById('progress').textContent=index+' / '+total;");
        html.append("document.getElementById('waiting').textContent=d.waiting;");
        html.append("document.getElementById('eating').textContent=d.eating;");
        html.append("document.getElementById('message').textContent=d.who+': '+d.message;");

        html.append("for(let i=0;i<4;i++){");
        html.append("let s=d.states[i],p=document.getElementById('person'+i),cls='thinking';");
        html.append("if(s==='HUNGRY')cls='hungry';");
        html.append("if(s==='EATING')cls='eating';");
        html.append("if(s==='FINISHED')cls='finished';");
        html.append("p.className='person '+cls+' p'+(i+1);");
        html.append("p.innerHTML='P'+(i+1)+'<span>'+s+'</span>';");

        html.append("let f=document.getElementById('fork'+i);");
        html.append("f.className='chopstick f'+(i+1)+(d.forks[i]?' used':'');");
        html.append("}");

        html.append("for(let i=0;i<total;i++){let e=document.getElementById('event'+i);");
        html.append("e.style.display=i<=n?'block':'none';");
        html.append("e.className=i===n?'event active':'event';}}");

        html.append("function nextEvent(){pause();if(index<total)showEvent(index);}");
        html.append("function play(){if(timer!==null)return;if(index>=total)resetSim();timer=setInterval(()=>{if(index<total)showEvent(index);else pause();},850);}");
        html.append("function pause(){if(timer!==null){clearInterval(timer);timer=null;}}");

        html.append("function resetSim(){pause();index=0;");
        html.append("document.getElementById('progress').textContent='0 / '+total;");
        html.append("document.getElementById('waiting').textContent='0';");
        html.append("document.getElementById('eating').textContent='0';");
        html.append("document.getElementById('message').textContent='Press Play or Next Event to start.';");

        html.append("for(let i=0;i<4;i++){let p=document.getElementById('person'+i);");
        html.append("p.className='person thinking p'+(i+1);");
        html.append("p.innerHTML='P'+(i+1)+'<span>THINKING</span>';");
        html.append("document.getElementById('fork'+i).className='chopstick f'+(i+1);}");
        html.append("for(let i=0;i<total;i++)document.getElementById('event'+i).style.display='none';}");

        html.append("</script></body></html>");

        writer.write(html.toString());
        writer.close();

        System.out.println(
                "\nHTML visualization generated: DiningPhilosophersMonitor.html"
        );
    }

    public static void main(String[] args) {
        Philosopher[] philosophers = new Philosopher[N];

        for (int i = 0; i < N; i++) {
            philosophers[i] = new Philosopher(i);
        }

        System.out.println("Starting monitor-based simulation...\n");

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
            createHTML(monitor.events);
        } catch (IOException e) {
            System.out.println("Could not generate HTML: " + e.getMessage());
        }
    }
}

