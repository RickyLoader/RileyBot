package COD.Assets;

import COD.MWManager;
import Network.NetworkRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Hold missing attachments and handle storing/retrieving from the database
 */
public class MissingWeaponAttachments {
    public static final String ATTACHMENT_URL = "modernwarfare/attachments/missing";
    public final Weapon weapon;
    public final ArrayList<String> attachmentCodenames;

    /**
     * Create a missing attachment
     *
     * @param weapon Weapon which attachment belongs to
     */
    public MissingWeaponAttachments(Weapon weapon) {
        this.weapon = weapon;
        this.attachmentCodenames = new ArrayList<>();
    }

    /**
     * Get the weapon that the missing attachment belongs to
     *
     * @return Weapon
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Get a list of codenames for the missing attachments
     *
     * @return Missing attachment codenames
     */
    public ArrayList<String> getAttachmentCodenames() {
        return attachmentCodenames;
    }

    /**
     * Add an attachment codename to the list of missing attachment codenames
     *
     * @param attachmentCodename Missing attachment codename
     */
    public void addMissingAttachment(String attachmentCodename) {
        this.attachmentCodenames.add(attachmentCodename);
    }


    /**
     * Get a list of the known missing attachments
     *
     * @return List of known missing weapon attachments
     */
    public static ArrayList<MissingWeaponAttachments> getMissingAttachments() {
        JSONArray missingJSON = new JSONArray(
                new NetworkRequest(ATTACHMENT_URL, true).get().body
        );
        HashMap<String, MissingWeaponAttachments> missing = new HashMap<>();
        for(int i = 0; i < missingJSON.length(); i++) {
            JSONObject info = missingJSON.getJSONObject(i);
            String weaponName = info.getString("weapon_name");
            MissingWeaponAttachments missingWeaponAttachments = missing.get(weaponName);
            if(missingWeaponAttachments == null) {
                String weaponCodename = info.getString("weapon_name");
                missingWeaponAttachments = new MissingWeaponAttachments(
                        MWManager.getInstance().getWeaponByCodename(
                                weaponCodename,
                                Weapon.getCategoryFromWeaponCodename(weaponCodename)
                        )
                );
            }
            missingWeaponAttachments.addMissingAttachment(info.getString("attachment_name"));
            missing.put(weaponName, missingWeaponAttachments);
        }
        return new ArrayList<>(missing.values());
    }

    /**
     * Asynchronously add an attachment to the database of missing attachments
     *
     * @param attachmentName Missing attachment name
     * @param weaponCodename Codename of weapon which attachment belongs to
     */
    public static void addMissingAttachment(String attachmentName, String weaponCodename) {
        new Thread(() -> new NetworkRequest(ATTACHMENT_URL, true).post(
                new JSONObject()
                        .put("attachment_name", attachmentName)
                        .put("weapon_name", weaponCodename)
                        .toString()
        )).start();
    }
}
