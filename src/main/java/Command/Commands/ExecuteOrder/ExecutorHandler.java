package Command.Commands.ExecuteOrder;

import java.util.ArrayList;
import java.util.Random;

public class ExecutorHandler {
    private final ArrayList<Executor> executors;

    public ExecutorHandler() {
        this.executors = createExecutors();
    }

    public Executor getRandomExecutor() {
        return executors.get(new Random().nextInt(executors.size()));
    }

    private ArrayList<Executor> createExecutors() {
        ArrayList<Executor> executors = new ArrayList<>();

        // Palpatine
        executors.add(new Executor(
                        "https://www.youtube.com/watch?v=rAfFSu-_3cA",
                        new String[]{
                                "https://vignette.wikia.nocookie.net/starwars/images/9/98/Palpatine-TROS-infobox.jpg",
                                "https://cdn.onebauer.media/one/empire-images/articles/5cb5b9fb133d503e3a48e86f/star-wars-palpatine.jpg",
                                "https://gamespot1.cbsistatic.com/uploads/original/1581/15811374/3522194-star%20wars%20emperor%20palpatine.jpeg"
                        }
                )
        );

        // Tactical nuke
        executors.add(new Executor(
                        "https://youtu.be/rV2l_WNd7Wo",
                        new String[]{
                                "https://vignette.wikia.nocookie.net/callofduty/images/4/41/Tacticle_Nuke_menu_icon_MW2.png"
                        }
                )
        );

        // Ice barrage
        executors.add(new Executor(
                        "https://www.youtube.com/watch?v=A8ZZmU8orvg",
                        new String[]{
                                "https://i.redd.it/3mag15dvnz111.jpg"
                        }
                )
        );

        // Weakest link
        executors.add(new Executor(
                        "https://www.youtube.com/watch?v=b_KYjfYjk0Q",
                        new String[]{
                                "https://keyassets-p2.timeincuk.net/wp/prod/wp-content/uploads/sites/42/2011/04/143994.jpg"
                        }
                )
        );

        // Survivor
        executors.add(new Executor(
                        "https://www.youtube.com/watch?v=NKqPBShXv8M",
                        new String[]{
                                "https://wwwimage-secure.cbsstatic.com/thumbnails/photos/w400-q80/cast/44dd3205083ba2dc_ff20df8317529a68_svr_cast_jprobst_800x1000.jpg",
                                "https://survivoroz.files.wordpress.com/2016/04/survivor-jeff-probst1.jpg"
                        }
                )
        );

        // Gordon
        executors.add(new Executor(
                        "https://www.youtube.com/watch?v=M_wxitPU54s",
                        new String[]{
                                "https://pyxis.nymag.com/v1/imgs/8d7/8d1/a6b94063a43171a380fb9c6b1c4da37f8f-20-gordon-ramsay.rsquare.w700.jpg",
                                "https://yt3.ggpht.com/a/AATXAJx5R7wPL-FXcTZvQ5wjgMNoj3F3wihflT_dKQVUdQ=s900-c-k-c0xffffffff-no-rj-mo",
                                "https://www.telegraph.co.uk/content/dam/news/2016/09/29/6455882-ramsay-news_trans_NvBQzQNjv4BqbRF8GMdXQ5UNQkWBrq_MOBxo7k3IcFzOpcVpLpEd-fY.jpg"
                        }
                )
        );
        return executors;
    }

    static class Executor {
        private final String track;
        private final String[] images;

        public Executor(String track, String[] images) {
            this.track = track;
            this.images = images;
        }

        public String getTrack() {
            return track;
        }

        public String getImage() {
            return images[new Random().nextInt(images.length)];
        }
    }
}
