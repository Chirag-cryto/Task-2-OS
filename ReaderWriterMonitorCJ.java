package ReaderWriterMonitor;

import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;

public class ReaderWriterMonitorCJ {

    static class Monitor {
        int activeReaders = 0;
        int waitingReaders = 0;
        int waitingWriters = 0;
        boolean writerActive = false;

        ArrayList<String> events = new ArrayList<String>();

        String resourceStatus() {
            if (writerActive)
                return "WRITING";
            else if (activeReaders > 0)
                return "READING";
            else
                return "AVAILABLE";
        }

        void log(String id, String state) {
            String event = id + " : " + state
                    + " | Active Readers: " + activeReaders
                    + " | Waiting Readers: " + waitingReaders
                    + " | Waiting Writers: " + waitingWriters
                    + " | Resource: " + resourceStatus();

            events.add(event);
            System.out.println(event);
        }

        synchronized void startReading(String id)
                throws InterruptedException {

            waitingReaders++;
            log(id, "WAITING");

            while (writerActive || waitingWriters > 0) {
                wait();
            }

            waitingReaders--;
            activeReaders++;

            log(id, "ENTERED READING SECTION");
        }

        synchronized void stopReading(String id) {
            activeReaders--;

            log(id, "EXITED READING SECTION");

            if (activeReaders == 0) {
                notifyAll();
            }
        }

        synchronized void startWriting(String id)
                throws InterruptedException {

            waitingWriters++;
            log(id, "WAITING");

            while (writerActive || activeReaders > 0) {
                wait();
            }

            waitingWriters--;
            writerActive = true;

            log(id, "ENTERED WRITING SECTION");
        }

        synchronized void stopWriting(String id) {
            writerActive = false;

            log(id, "EXITED WRITING SECTION");

            notifyAll();
        }
    }

    static class Reader extends Thread {
        Monitor monitor;
        String id;

        Reader(Monitor monitor, String id) {
            this.monitor = monitor;
            this.id = id;
        }

        public void run() {
            try {
                monitor.startReading(id);

                Thread.sleep(1500);

                monitor.stopReading(id);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Writer extends Thread {
        Monitor monitor;
        String id;

        Writer(Monitor monitor, String id) {
            this.monitor = monitor;
            this.id = id;
        }

        public void run() {
            try {
                monitor.startWriting(id);

                Thread.sleep(2000);

                monitor.stopWriting(id);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }


static void createHTML(ArrayList<String> events) throws IOException {

    PrintWriter out = new PrintWriter("ReadersWritersMonitor.html");

    out.println("<!DOCTYPE html>");
    out.println("<html><head><title>Readers-Writers Monitor</title>");
    out.println("<meta charset='UTF-8'>");

    out.println("<style>");
    out.println("body{font-family:Arial;background:#eef2f7;margin:0;color:#1e293b}");
    out.println("header{background:#172554;color:white;padding:22px;text-align:center}");
    out.println(".container{max-width:1100px;margin:25px auto;padding:0 15px}");
    out.println(".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:15px}");
    out.println(".card{background:white;padding:18px;border-radius:10px;box-shadow:0 2px 8px #0001}");
    out.println(".number{font-size:28px;font-weight:bold;margin-top:10px}");
    out.println(".resource{text-align:center;border-top:5px solid #64748b}");
    out.println(".process{border-left:5px solid #94a3b8;margin-top:10px}");
    out.println(".active{border-left-color:#16a34a}");
    out.println(".waiting{border-left-color:#f59e0b}");
    out.println(".finished{border-left-color:#2563eb}");
    out.println(".controls{margin:20px 0}");
    out.println("button{padding:10px 18px;margin:4px;border:0;border-radius:6px;background:#1d4ed8;color:white;cursor:pointer}");
    out.println("button:hover{background:#1e40af}");
    out.println(".event{background:white;padding:14px;margin:10px 0;border-radius:7px;border-left:5px solid #94a3b8}");
    out.println(".event.current{background:#dbeafe;border-left-color:#2563eb}");
    out.println(".small{color:#64748b;font-size:13px}");
    out.println("</style></head><body>");

    out.println("<header>");
    out.println("<h1>Readers-Writers Simulation</h1>");
    out.println("<p>Monitor-Based Synchronization in Java</p>");
    out.println("</header>");

    out.println("<div class='container'>");

    out.println("<div class='card resource'>");
    out.println("<h3>Shared Resource</h3>");
    out.println("<div class='number' id='resource'>AVAILABLE</div>");
    out.println("<p id='description'>Waiting to start simulation</p>");
    out.println("</div>");

    out.println("<h2>System Status</h2>");
    out.println("<div class='grid'>");

    out.println("<div class='card'>Active Readers<div class='number' id='ar'>0</div></div>");
    out.println("<div class='card'>Waiting Readers<div class='number' id='wr'>0</div></div>");
    out.println("<div class='card'>Waiting Writers<div class='number' id='ww'>0</div></div>");
    out.println("<div class='card'>Writer Active<div class='number' id='wa'>No</div></div>");

    out.println("</div>");

    out.println("<h2>Reader and Writer Processes</h2>");
    out.println("<div class='grid'>");

    String[] names = {
        "Reader-1", "Reader-2", "Reader-3",
        "Writer-1", "Writer-2"
    };

    for (int i = 0; i < names.length; i++) {
        out.println("<div class='card process' id='" + names[i] + "'>");
        out.println("<h3>" + names[i] + "</h3>");
        out.println("<p class='processState'>NOT STARTED</p>");
        out.println("</div>");
    }

    out.println("</div>");

    out.println("<div class='card controls'>");
    out.println("<h2>Execution Controls</h2>");
    out.println("<button onclick='nextEvent()'>Next Event</button>");
    out.println("<button onclick='play()'>Play</button>");
    out.println("<button onclick='pause()'>Pause</button>");
    out.println("<button onclick='resetEvents()'>Reset</button>");
    out.println("<p id='progress'>Ready</p>");
    out.println("<p class='small'>Each step represents an event recorded by the Java program.</p>");
    out.println("</div>");

    out.println("<h2>Execution Timeline</h2>");
    out.println("<div id='timeline'>");

    for (int i = 0; i < events.size(); i++) {

        String[] parts = events.get(i).split("\\|");
        String[] first = parts[0].trim().split(" : ", 2);

        String actor = first[0].trim();
        String state = first[1].trim();

        String ar = parts[1].trim().substring("Active Readers:".length()).trim();
        String wr = parts[2].trim().substring("Waiting Readers:".length()).trim();
        String ww = parts[3].trim().substring("Waiting Writers:".length()).trim();
        String rs = parts[4].trim().substring("Resource:".length()).trim();

        out.println("<div class='event' style='display:none'");
        out.println(" data-actor='" + escapeHTML(actor) + "'");
        out.println(" data-state='" + escapeHTML(state) + "'");
        out.println(" data-ar='" + ar + "'");
        out.println(" data-wr='" + wr + "'");
        out.println(" data-ww='" + ww + "'");
        out.println(" data-resource='" + rs + "'>");

        out.println("<b>Event " + (i + 1) + "</b><br>");
        out.println(escapeHTML(events.get(i)));
        out.println("</div>");
    }

    out.println("</div></div>");

    out.println("<script>");

    out.println("let current=0;");
    out.println("let timer=null;");
    out.println("let items=document.getElementsByClassName('event');");

    out.println("function showEvent(){");
    out.println("if(current>=items.length){pause();return;}");
    out.println("let e=items[current];");
    out.println("e.style.display='block';");
    out.println("e.classList.add('current');");

    out.println("document.getElementById('resource').innerText=e.dataset.resource;");
    out.println("document.getElementById('ar').innerText=e.dataset.ar;");
    out.println("document.getElementById('wr').innerText=e.dataset.wr;");
    out.println("document.getElementById('ww').innerText=e.dataset.ww;");
    out.println("document.getElementById('wa').innerText=e.dataset.resource==='WRITING'?'Yes':'No';");

    out.println("document.getElementById('description').innerText=e.dataset.actor+' - '+e.dataset.state;");
    out.println("document.getElementById('progress').innerText='Event '+(current+1)+' of '+items.length;");

    out.println("let p=document.getElementById(e.dataset.actor);");
    out.println("if(p){");
    out.println("let s=p.getElementsByClassName('processState')[0];");
    out.println("let state=e.dataset.state;");
    out.println("s.innerText=state;");
    out.println("p.classList.remove('active','waiting','finished');");
    out.println("if(state==='WAITING')p.classList.add('waiting');");
    out.println("else if(state.indexOf('ENTERED')>=0)p.classList.add('active');");
    out.println("else if(state.indexOf('EXITED')>=0)p.classList.add('finished');");
    out.println("}");

    out.println("current++;");
    out.println("}");

    out.println("function nextEvent(){");
    out.println("if(current<items.length)showEvent();");
    out.println("else pause();");
    out.println("}");

    out.println("function play(){");
    out.println("if(timer===null){");
    out.println("timer=setInterval(nextEvent,1200);");
    out.println("}}");

    out.println("function pause(){");
    out.println("clearInterval(timer);");
    out.println("timer=null;");
    out.println("}");

    out.println("function resetEvents(){");
    out.println("pause();current=0;");
    out.println("for(let i=0;i<items.length;i++){");
    out.println("items[i].style.display='none';");
    out.println("items[i].classList.remove('current');");
    out.println("}");

    out.println("let p=document.getElementsByClassName('process');");
    out.println("for(let i=0;i<p.length;i++){");
    out.println("p[i].classList.remove('active','waiting','finished');");
    out.println("p[i].getElementsByClassName('processState')[0].innerText='NOT STARTED';");
    out.println("}");

    out.println("document.getElementById('resource').innerText='AVAILABLE';");
    out.println("document.getElementById('description').innerText='Waiting to start simulation';");
    out.println("document.getElementById('ar').innerText='0';");
    out.println("document.getElementById('wr').innerText='0';");
    out.println("document.getElementById('ww').innerText='0';");
    out.println("document.getElementById('wa').innerText='No';");
    out.println("document.getElementById('progress').innerText='Ready';");
    out.println("}");

    out.println("</script></body></html>");

    out.close();

    System.out.println("Realistic HTML simulation created successfully.");
}


    public static void main(String[] args) throws Exception {

        Monitor monitor = new Monitor();

        Reader r1 = new Reader(monitor, "Reader-1");
        Reader r2 = new Reader(monitor, "Reader-2");
        Reader r3 = new Reader(monitor, "Reader-3");

        Writer w1 = new Writer(monitor, "Writer-1");
        Writer w2 = new Writer(monitor, "Writer-2");

        r1.start();
        r2.start();

        Thread.sleep(200);

        w1.start();
        r3.start();
        w2.start();

        r1.join();
        r2.join();
        r3.join();
        w1.join();
        w2.join();

        createHTML(monitor.events);
    }
}

