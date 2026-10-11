package ReaderWriterProgram;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class ReaderWriterCJ {

    static Semaphore mutex = new Semaphore(1);
    static Semaphore resource = new Semaphore(1);
    static int readers = 0;
    static ArrayList<String> events = new ArrayList<>();
    static int activeReaders = 0;
    static int waitingReaders = 0;
    static int waitingWriters = 0;
    static boolean writerActive = false;

    
    static synchronized void log(String id, String state) {
        String resourceState = "Available";

        if (writerActive) {
            resourceState = "Writting";
        } else if (activeReaders > 0) {
            resourceState = "Reading";
        }

        String event = id + " : " + state
                + " Active readers: " + activeReaders
                + " Waiting readers: " + waitingReaders
                + " Waiting writers: " + waitingWriters
                + " Resource: " + resourceState;

        events.add(event);
        System.out.println(event);
    }

    static class Reader extends Thread {
        String id;

        Reader(String id) {
            this.id = id;
        }

        public void run() {
            try {
                synchronized (ReaderWriterCJ.class) {
                    waitingReaders++;
                    log(id, "Waitting");
                }

                mutex.acquire();
                readers++;

                if (readers == 1) {
                    resource.acquire();
                }

                synchronized (ReaderWriterCJ.class) {
                    waitingReaders--;
                    activeReaders++;
                    log(id, "Entered reading Section");
                }

                mutex.release();

                Thread.sleep(1500);

                mutex.acquire();

                synchronized (ReaderWriterCJ.class) {
                    activeReaders--;
                }

                readers--;

                if (readers == 0) {
                    resource.release();
                }

                synchronized (ReaderWriterCJ.class) {
                    log(id, "Exited reading section");
                }

                mutex.release();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Writer extends Thread {
        String id;

        Writer(String id) {
            this.id = id;
        }

        public void run() {
            try {
                synchronized (ReaderWriterCJ.class) {
                    waitingWriters++;
                    log(id, "Waitting");
                }

                resource.acquire();

                synchronized (ReaderWriterCJ.class) {
                    waitingWriters--;
                    writerActive = true;
                    log(id, "Enter writing section");
                }

                Thread.sleep(2000);

                synchronized (ReaderWriterCJ.class) {
                    writerActive = false;
                    log(id, "Exited writting section");
                }

                resource.release();

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

    static void createHTML() throws IOException {
        PrintWriter out = new PrintWriter(
                "ReaderWriterCJ.html");

        out.println("<html><head><title>Readers Writers</title>");
        out.println("<style>");
        out.println("body{font-family:Arial;margin:30px;background:#f2f2f2}");
        out.println(".event{padding:12px;margin:8px;background:white;border-left:5px solid #2563eb}");
        out.println("button{padding:10px;margin:5px;cursor:pointer}");
        out.println("</style></head><body>");

        out.println("<h1>Readers-Writers Simulation</h1>");
        out.println("<p>Events recorded by the Java program</p>");
        out.println("<button onclick='nextEvent()'>Next Event</button>");
        out.println("<button onclick='play()'>Play</button>");
        out.println("<button onclick='pause()'>Pause</button>");
        out.println("<button onclick='resetEvents()'>Reset</button>");
        out.println("<h3 id='status'>Ready</h3>");

        out.println("<div id='timeline'>");

        for (int i = 0; i < events.size(); i++) {
            out.println("<div class='event' style='display:none'>"
                    + (i + 1) + ". " + escapeHTML(events.get(i))
                    + "</div>");
        }

        out.println("</div>");

        out.println("<script>");
        out.println("let current=0;");
        out.println("let timer=null;");
        out.println("let items=document.getElementsByClassName('event');");

        out.println("function nextEvent(){");
        out.println("if(current<items.length){");
        out.println("items[current].style.display='block';");
        out.println("current++;");
        out.println("document.getElementById('status').innerText="
                + "'Event '+current+' of '+items.length;");
        out.println("}else{pause();}}");

        out.println("function play(){");
        out.println("if(timer===null){");
        out.println("timer=setInterval(function(){");
        out.println("if(current<items.length){nextEvent();}");
        out.println("else{pause();}");
        out.println("},1000);}}");

        out.println("function pause(){clearInterval(timer);timer=null;}");

        out.println("function resetEvents(){");
        out.println("pause();current=0;");
        out.println("for(let i=0;i<items.length;i++){");
        out.println("items[i].style.display='none';}");
        out.println("document.getElementById('status').innerText='Ready';}");

        out.println("</script></body></html>");

        out.close();
        System.out.println("HTML simulation created successfully.");
    }

    public static void main(String[] args) throws Exception {

        Reader r1 = new Reader("Reader1");
        Reader r2 = new Reader("Reader2");
        Reader r3 = new Reader("Reader3");
        Writer w1 = new Writer("Writer1");
        Writer w2 = new Writer("Writer2");

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

        createHTML();
    }
}


