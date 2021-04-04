package Command.Commands;

import Command.Structure.*;
import Network.ImgurManager;
import Network.NetworkRequest;
import Photography.Photo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class PhotographyCommand extends OnReadyDiscordCommand {
    private final HashMap<Integer, Photo> photos;
    private final HashMap<Long, Photo> sentPhotos;
    private final Random rand;

    public PhotographyCommand() {
        super(
                "photography",
                "Cool photography!",
                "photography \nphotography [attached image]"
        );
        this.photos = fetchPhotos();
        this.rand = new Random();
        this.sentPhotos = new HashMap<>();
    }

    /**
     * Fetch the photos from the database
     *
     * @return Map of photo id -> photo
     */
    private HashMap<Integer, Photo> fetchPhotos() {
        HashMap<Integer, Photo> photos = new HashMap<>();
        JSONArray results = new JSONArray(
                new NetworkRequest("photography/get", true).get().body
        );
        for(int i = 0; i < results.length(); i++) {
            JSONObject photo = results.getJSONObject(i);
            int id = photo.getInt("id");
            photos.put(
                    id,
                    new Photo(id, photo.getString("url"))
            );
        }
        return photos;
    }

    @Override
    public void execute(CommandContext context) {
        MessageChannel channel = context.getMessageChannel();
        Message message = context.getMessage();
        Member member = context.getMember();
        List<Attachment> attachments = message.getAttachments();

        if(attachments.isEmpty()) {
            if(photos.isEmpty()) {
                channel.sendMessage(
                        member.getAsMention() + " Sorry bro, I've got no photos to display!\n" + getHelpNameCoded()
                ).queue();
            }
            else {
                Photo photo = getRandomPhoto();
                channel.sendMessage(photo.getUrl()).queue(photoMessage -> sentPhotos.put(photoMessage.getIdLong(), photo));
            }
            return;
        }
        savePhoto(message, attachments.get(0), channel);
    }

    /**
     * Delete the given photo from the database
     *
     * @param photo Photo to delete
     * @return Photo was deleted
     */
    private boolean deletePhoto(Photo photo) {
        JSONObject response = new JSONObject(
                new NetworkRequest("photography/delete/" + photo.getId(), true).delete().body
        );
        return response.getBoolean("status") && response.getInt("affected") != 0;
    }

    /**
     * Attempt to save the given attachment to the database and reply to the given message with the result.
     * If the upload is successful, add the photo to the map for later retrieval.
     * Non photo attachments will be ignored.
     *
     * @param message    Message containing photo to upload
     * @param attachment Attachment to save
     * @param channel    Channel to send typing to
     */
    private void savePhoto(Message message, Attachment attachment, MessageChannel channel) {
        new Thread(() -> {
            if(!attachment.isImage()) {
                message.reply("I'm not saving that.").queue();
                return;
            }
            channel.sendTyping().queue();
            Photo photo = uploadPhoto(attachment.getUrl());
            if(photo == null) {
                message.reply("I'm so sorry, I couldn't save this photo, please forgive me").queue();
                return;
            }
            photos.put(photo.getId(), photo);
            message.delete().queue(deleted -> channel.sendMessage(
                    "Photo added to the photography command:\n" + photo.getUrl()
            ).queue());
        }).start();
    }

    /**
     * Save a photo to the database (upload to Imgur first for permanent image hosting)
     *
     * @param url Discord url of photo
     * @return Photo
     */
    private Photo uploadPhoto(String url) {
        try {
            String imgurUrl = ImgurManager.uploadImage(url);
            JSONObject body = new JSONObject().put("url", imgurUrl);
            JSONObject response = new JSONObject(
                    new NetworkRequest("photography/add", true).post(body.toString()).body
            );
            return new Photo(response.getInt("id"), imgurUrl);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Get a random photo from the map
     *
     * @return Random photo
     */
    private Photo getRandomPhoto() {
        ArrayList<Photo> photos = new ArrayList<>(this.photos.values());
        return photos.get(rand.nextInt(photos.size()));
    }

    /**
     * Register the emote listener for saving/deleting photos
     *
     * @param jda         JDA for registering listener
     * @param emoteHelper Emote helper
     */
    @Override
    public void onReady(JDA jda, EmoteHelper emoteHelper) {
        Emote saveEmote = emoteHelper.getSavePhoto();
        Emote deleteEmote = emoteHelper.getDeletePhoto();
        jda.addEventListener(new EmoteListener() {
            @Override
            public void handleReaction(MessageReaction reaction, User user, Guild guild) {
                Emote emote = reaction.getReactionEmote().getEmote();
                long messageId = reaction.getMessageIdLong();
                MessageChannel channel = reaction.getChannel();

                channel.retrieveMessageById(messageId).queue(message -> {
                    if(emote == saveEmote) {
                        List<Attachment> attachments = message.getAttachments();
                        if(message.getAuthor() == guild.getSelfMember().getUser() || attachments.isEmpty()) {
                            message.reply("No can do!").queue();
                            return;
                        }
                        savePhoto(message, attachments.get(0), channel);
                    }
                    else if(emote == deleteEmote) {
                        Photo toDelete = sentPhotos.get(messageId);
                        if(toDelete == null) {
                            return;
                        }
                        channel.sendTyping().queue();
                        if(deletePhoto(toDelete)) {
                            sentPhotos.remove(messageId);
                            photos.remove(toDelete.getId());
                            message.reply("Photo removed from the photography command!").queue();
                        }
                        else {
                            message.reply("I couldn't delete that photo!").queue();
                        }
                    }
                });
            }
        });
    }
}
