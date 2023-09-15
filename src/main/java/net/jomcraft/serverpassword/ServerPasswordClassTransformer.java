package net.jomcraft.serverpassword;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;
import java.io.IOException;
import java.util.Arrays;

public class ServerPasswordClassTransformer implements IClassTransformer {

    private static final String[] classesToTransform = {

            "net.minecraft.server.management.ServerConfigurationManager", "net.minecraft.client.multiplayer.GuiConnecting",

    };

    public static boolean gameObf;

    @Override
    public byte[] transform(String name, String transformedName, byte[] classToTransform) {
        boolean isObfuscated = !name.equals(transformedName);

        int c_index = Arrays.asList(classesToTransform).indexOf(transformedName);
        if (!gameObf && isObfuscated) gameObf = true;

        return c_index != -1 ? transform(c_index, classToTransform, isObfuscated, name, transformedName) : classToTransform;
    }

    private byte[] transform(int c_index, byte[] classToTransform, boolean isObfuscated, String name, String transformedName) {

        ServerPassword.log(Level.INFO, "Transforming now: " + classesToTransform[c_index]);

        try {

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classToTransform);
            classReader.accept(classNode, 0);

            switch (c_index) {
                case 0:
                    if (ServerPassword.isDedi()) {

                        if (ServerPassword.isCauldron()) {
                            transformPlayerListC(classNode, isObfuscated);

                        } else {
                            transformPlayerList(classNode, isObfuscated);
                        }
                    }
                case 1:
                    transformGui(classNode, isObfuscated);
                    break;

            }

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            return classWriter.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classToTransform;
    }

    private static void transformGui(ClassNode mainClass, boolean isObfuscated) {
        final String CLASS_NAME = isObfuscated ? "a" : "func_146367_a";
        final String CLASS_NAME_OBF = isObfuscated ? "(Ljava/lang/String;I)V" : "(Ljava/lang/String;I)V";

        for (MethodNode method : mainClass.methods) {

            if (method.name.equals(CLASS_NAME) && method.desc.equals(CLASS_NAME_OBF)) {

                mainClass.methods.remove(method);

                MethodNode mv = new MethodNode(ACC_PRIVATE, (gameObf ? "a" : "func_146367_a"), (gameObf ? "(Ljava/lang/String;I)V" : "(Ljava/lang/String;I)V"), null, null);

                Label l0 = new Label();
                mv.visitLabel(l0);

                mv.visitVarInsn(ALOAD, 0);

                mv.visitFieldInsn(GETSTATIC, "net/minecraft/client/multiplayer/GuiConnecting", (gameObf ? "field_146370_f" : "logger"), "Lorg/apache/logging/log4j/Logger;");
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitFieldInsn(GETSTATIC, "net/minecraft/client/multiplayer/GuiConnecting", (gameObf ? "field_146372_a" : "field_146372_a"), "Ljava/util/concurrent/atomic/AtomicInteger;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/client/multiplayer/GuiConnecting", (gameObf ? "field_146297_k" : "mc"), "Lnet/minecraft/client/Minecraft;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/client/multiplayer/GuiConnecting", (gameObf ? "field_146374_i" : "field_146374_i"), "Lnet/minecraft/client/gui/GuiScreen;");

                mv.visitMethodInsn(INVOKESTATIC, "net/jomcraft/serverpassword/EncryptPatch", "connect2", "(Lnet/minecraft/client/multiplayer/GuiConnecting;Lorg/apache/logging/log4j/Logger;Ljava/lang/String;ILjava/util/concurrent/atomic/AtomicInteger;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiScreen;)V", false);

                Label l1 = new Label();
                mv.visitLabel(l1);
                mv.visitInsn(RETURN);

                mainClass.methods.add(mv);

                System.out.println("Transformed gui!");
                break;

            }
        }

    }

    private static void transformPlayerList(ClassNode mainClass, boolean isObfuscated) {
        final String CLASS_NAME = isObfuscated ? "a" : "allowUserToConnect";
        final String CLASS_NAME_OBF = isObfuscated ? "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;" : "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;";

        for (MethodNode method : mainClass.methods) {

            if (method.name.equals(CLASS_NAME) && method.desc.equals(CLASS_NAME_OBF)) {

                mainClass.methods.remove(method);

                MethodNode mv = new MethodNode(ACC_PUBLIC, (gameObf ? "a" : "allowUserToConnect"), (gameObf ? "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;" : "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;"), null, null);

                Label l0 = new Label();
                mv.visitLabel(l0);

                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72409_l" : "whiteListEnforced"), "Z");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72414_i" : "ops"), "Lnet/minecraft/server/management/UserListOps;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72411_j" : "whiteListedPlayers"), "Lnet/minecraft/server/management/UserListWhitelist;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72401_g" : "bannedPlayers"), "Lnet/minecraft/server/management/UserListBans;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72413_h" : "bannedIPs"), "Lnet/minecraft/server/management/BanList;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72404_b" : "playerEntityList"), "Ljava/util/List;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "net/minecraft/server/management/ServerConfigurationManager", (gameObf ? "field_72405_c" : "maxPlayers"), "I");

                mv.visitMethodInsn(INVOKESTATIC, "net/jomcraft/serverpassword/PatchMethods", "allowUserToConnect", "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/server/management/UserListOps;Lnet/minecraft/server/management/UserListWhitelist;Lnet/minecraft/server/management/UserListBans;Lnet/minecraft/server/management/BanList;Ljava/util/List;I)Ljava/lang/String;", false);

                mv.visitInsn(ARETURN);

                System.out.println("Transformed playerlist!");

                mainClass.methods.add(mv);

                break;

            }
        }

    }

    private static void transformPlayerListC(ClassNode mainClass, boolean isObfuscated) throws IOException {
        final String CLASS_NAME = "attemptLogin";
        final String CLASS_NAME_OBF = "(Lnn;Lcom/mojang/authlib/GameProfile;Ljava/lang/String;)Lmw;";

        for (MethodNode method : mainClass.methods) {

            if (method.name.equals(CLASS_NAME) && method.desc.equals(CLASS_NAME_OBF)) {

                AbstractInsnNode targetNode = null;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {

                    if (instruction.getOpcode() == INVOKEVIRTUAL) {

                        if (((MethodInsnNode) instruction).name.equals("getAddress") && instruction.getPrevious().getOpcode() == 192 && instruction.getNext().getOpcode() == 183) {

                            targetNode = instruction.getNext().getNext().getNext();
                            break;

                        }

                    }

                }

                if (targetNode != null) {

                    // 1
                    LabelNode newLabelNode = new LabelNode();
                    InsnList toInsert = new InsnList();
                    toInsert.add(new MethodInsnNode(INVOKESTATIC, "net/jomcraft/serverpassword/ServerPassword", "isDedi", "()Z", false));

                    method.instructions.insertBefore(targetNode, toInsert);

                    LabelNode newLabelNodeby = new LabelNode();
                    InsnList toInsertby = new InsnList();
                    toInsertby.add(new JumpInsnNode(IFEQ, newLabelNodeby));

                    method.instructions.insertBefore(targetNode, toInsertby);

                    // 2
                    LabelNode newLabelNode2 = new LabelNode();

                    InsnList toInsert2 = new InsnList();

                    toInsert2.add(new VarInsnNode(ALOAD, 4));
                    toInsert2.add(new VarInsnNode(ALOAD, 2));
                    toInsert2.add(new MethodInsnNode(INVOKESTATIC, "net/jomcraft/serverpassword/PatchMethods", "allowUser", "(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;", false));
                    toInsert2.add(new VarInsnNode(ASTORE, 8));

                    method.instructions.insertBefore(targetNode, toInsert2);

                    // 3
                    LabelNode newLabelNode3 = new LabelNode();

                    InsnList toInsert3 = new InsnList();

                    toInsert3.add(new VarInsnNode(ALOAD, 8));
                    toInsert3.add(new JumpInsnNode(IFNULL, newLabelNodeby));

                    method.instructions.insertBefore(targetNode, toInsert3);

                    // 5
                    LabelNode newLabelNode5 = new LabelNode();

                    InsnList toInsert5 = new InsnList();

                    toInsert5.add(new VarInsnNode(ALOAD, 7));
                    toInsert5.add(new FieldInsnNode(GETSTATIC, "org/bukkit/event/player/PlayerLoginEvent$Result", "KICK_BANNED", "Lorg/bukkit/event/player/PlayerLoginEvent$Result;"));

                    toInsert5.add(new VarInsnNode(ALOAD, 8));
                    toInsert5.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/event/player/PlayerLoginEvent", "disallow", "(Lorg/bukkit/event/player/PlayerLoginEvent$Result;Ljava/lang/String;)V", false));

                    method.instructions.insertBefore(targetNode, toInsert5);

                    method.instructions.insert(targetNode, newLabelNode);
                    method.instructions.insert(targetNode, newLabelNodeby);
                    method.instructions.insert(targetNode, newLabelNode2);
                    method.instructions.insert(targetNode, newLabelNode3);
                    method.instructions.insert(targetNode, newLabelNode5);

                }

                System.out.println("Transformed playerlist! -> CAULDRON");
                break;
            }
        }

    }

}
