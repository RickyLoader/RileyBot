package TheHub;

/**
 * Highly respected performer of entertainment
 */
public class Performer {
    private final String name, image, desc, url, views, subscribers, gender;
    private final int rank, age;
    private final PROFILE_TYPE type;

    public enum PROFILE_TYPE {
        PORNSTAR,
        MODEL
    }

    /**
     * Create a Performer from a PerformerBuilder
     *
     * @param builder PerformerBuilder
     */
    private Performer(PerformerBuilder builder) {
        this.name = builder.name;
        this.image = builder.image;
        this.desc = builder.desc;
        this.url = builder.url;
        this.rank = builder.rank;
        this.views = builder.views;
        this.subscribers = builder.subscribers;
        this.gender = builder.gender;
        this.type = builder.type;
        this.age = builder.age;
    }

    /**
     * Get the total number of subscribers
     *
     * @return Total subscribers
     */
    public String getSubscribers() {
        return subscribers;
    }

    /**
     * Get the age of the performer
     *
     * @return Age
     */
    public int getAge() {
        return age;
    }

    /**
     * Check if the performer has a known gender
     *
     * @return Performer has known gender
     */
    public boolean hasGender() {
        return gender != null;
    }

    /**
     * Check if the performer has a known age
     *
     * @return Performer has a known age
     */
    public boolean hasAge() {
        return age > 0;
    }

    /**
     * Get the gender of the performer
     *
     * @return Gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Get the total number of video views
     *
     * @return Total video views
     */
    public String getViews() {
        return views;
    }

    /**
     * Get the bio description of the performer
     *
     * @return Bio description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Get the image URL of the performer
     *
     * @return image URL
     */
    public String getImage() {
        return image;
    }

    /**
     * Get the profile type of the performer
     *
     * @return Profile type
     */
    public PROFILE_TYPE getType() {
        return type;
    }

    /**
     * Get the name of the performer
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the overall rank of the performer
     *
     * @return Overall rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Get the URL to the performer's page
     *
     * @return URL
     */
    public String getURL() {
        return url;
    }

    /**
     * Build a Performer
     */
    public static class PerformerBuilder {
        private String name, image, desc, url, views, subscribers, gender;
        private PROFILE_TYPE type;
        private int rank, age;

        /**
         * Set the profile type of the performer
         *
         * @param type Profile type
         * @return Builder
         */
        public PerformerBuilder setType(PROFILE_TYPE type) {
            this.type = type;
            return this;
        }

        /**
         * Set the name of the performer
         *
         * @param name Name
         * @return Builder
         */
        public PerformerBuilder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the age of the performer
         *
         * @param age Age
         * @return Builder
         */
        public PerformerBuilder setAge(int age) {
            this.age = age;
            return this;
        }

        /**
         * Set the image URL for the performer
         *
         * @param image Image URL
         * @return Builder
         */
        public PerformerBuilder setImage(String image) {
            this.image = image;
            return this;
        }

        /**
         * Set the bio description of the performer
         *
         * @param desc Bio description
         * @return Builder
         */
        public PerformerBuilder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        /**
         * Set the URL to the performer's page
         *
         * @param url URL to performer's page
         * @return Builder
         */
        public PerformerBuilder setURL(String url) {
            this.url = url;
            return this;
        }

        /**
         * Set the overall rank of the performer
         *
         * @param rank Overall rank
         * @return Builder
         */
        public PerformerBuilder setRank(int rank) {
            this.rank = rank;
            return this;
        }

        /**
         * Set the number of total video views - 1,000 / 1.5M etc
         *
         * @param views Total video views
         * @return Builder
         */
        public PerformerBuilder setViews(String views) {
            this.views = views;
            return this;
        }

        /**
         * Set the gender of the Performer
         *
         * @param gender Gender
         * @return Builder
         */
        public PerformerBuilder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        /**
         * Set the number of subscribers - 1,000 / 1.5M etc
         *
         * @param subscribers Number of subscribers
         * @return Builder
         */
        public PerformerBuilder setSubscribers(String subscribers) {
            this.subscribers = subscribers;
            return this;
        }

        /**
         * Create the Performer
         *
         * @return Performer from builder values
         */
        public Performer build() {
            if(name == null || type == null) {
                throw new IllegalStateException("A performer must have a name and profile type!");
            }
            return new Performer(this);
        }
    }
}
