package fr.azures04.mods.multiauth.helpers;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class PlayerHelper {

    public static boolean isUsernameTakenOnServer(String username) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
        	return false;
        }

        if (server.getPlayerList().getPlayerByUsername(username) != null) {
        	return true;
        }

        GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(username);
        return profile != null;
    }

    public static boolean isUUIDTakenOnServer(UUID id) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
        	return false;
        }

        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(id);
        return player != null;
    }

    public static UUID parseUUID(String uuidWithoutDash) {
        return UUID.fromString(uuidWithoutDash.replaceFirst(
            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
            "$1-$2-$3-$4-$5"
        ));
    }
}