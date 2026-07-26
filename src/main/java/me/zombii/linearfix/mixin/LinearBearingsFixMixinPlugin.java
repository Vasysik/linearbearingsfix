package me.zombii.linearfix.mixin;

import java.util.List;
import java.util.Set;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Removes client-only bootstrap instructions from Linear Bearing's mod
 * constructor on dedicated servers.
 *
 * <p>The old implementation used a cancellable {@code @Inject} against
 * {@code <init>}. Mixin rejects cancellable constructor injections. Rewriting
 * only the unsafe instructions keeps the original constructor and all
 * server-safe registrations intact.</p>
 */
public final class LinearBearingsFixMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Linear Bearings Fix");

    private static final String TARGET_CLASS = "com.bearing.linearbearing.LinearBearing";
    private static final String TARGET_CONSTRUCTOR_DESCRIPTOR =
            "(Lnet/neoforged/bus/api/IEventBus;)V";

    private static final String CLIENT_MOD_HANDLER =
            "com/bearing/linearbearing/registrate/ClientModHandler";
    private static final String LINEAR_BEARING_CLIENT =
            "com/bearing/linearbearing/LinearBearingClient";
    private static final String EVENT_BUS = "net/neoforged/bus/api/IEventBus";

    private static final int EXPECTED_CLIENT_LISTENERS = 2;
    private static final int EXPECTED_CLIENT_INITIALIZERS = 1;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo) {

        if (!TARGET_CLASS.equals(targetClassName)
                || FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            return;
        }

        MethodNode constructor = targetClass.methods.stream()
                .filter(method -> "<init>".equals(method.name))
                .filter(method -> TARGET_CONSTRUCTOR_DESCRIPTOR.equals(method.desc))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find Linear Bearing's IEventBus constructor"));

        int removedListeners = removeClientListenerRegistrations(constructor);
        int removedInitializers = removeClientInitializers(constructor);

        if (removedListeners != EXPECTED_CLIENT_LISTENERS
                || removedInitializers != EXPECTED_CLIENT_INITIALIZERS) {
            throw new IllegalStateException(
                    "Unexpected Linear Bearing 1.3.5 constructor layout: removed "
                            + removedListeners + "/" + EXPECTED_CLIENT_LISTENERS
                            + " client listeners and "
                            + removedInitializers + "/" + EXPECTED_CLIENT_INITIALIZERS
                            + " client initializers");
        }

        LOGGER.info(
                "Patched Linear Bearing for dedicated server: removed {} client listeners and {} client initializer",
                removedListeners,
                removedInitializers);
    }

    private static int removeClientListenerRegistrations(MethodNode constructor) {
        int removed = 0;
        AbstractInsnNode instruction = constructor.instructions.getFirst();

        while (instruction != null) {
            AbstractInsnNode next = instruction.getNext();

            if (instruction instanceof InvokeDynamicInsnNode invokedynamic
                    && referencesOwner(invokedynamic, CLIENT_MOD_HANDLER)) {

                AbstractInsnNode eventBusLoad = previousOpcode(instruction);
                AbstractInsnNode addListenerCall = nextOpcode(instruction);

                if (!(eventBusLoad instanceof VarInsnNode variable)
                        || variable.getOpcode() != Opcodes.ALOAD
                        || !(addListenerCall instanceof MethodInsnNode method)
                        || !EVENT_BUS.equals(method.owner)
                        || !"addListener".equals(method.name)) {
                    throw new IllegalStateException(
                            "Could not identify the IEventBus.addListener sequence around a client handler");
                }

                AbstractInsnNode continueAt = addListenerCall.getNext();
                constructor.instructions.remove(eventBusLoad);
                constructor.instructions.remove(instruction);
                constructor.instructions.remove(addListenerCall);

                removed++;
                instruction = continueAt;
                continue;
            }

            instruction = next;
        }

        return removed;
    }

    private static int removeClientInitializers(MethodNode constructor) {
        int removed = 0;
        AbstractInsnNode instruction = constructor.instructions.getFirst();

        while (instruction != null) {
            AbstractInsnNode next = instruction.getNext();

            if (instruction instanceof MethodInsnNode method
                    && LINEAR_BEARING_CLIENT.equals(method.owner)
                    && "registerClient".equals(method.name)) {

                AbstractInsnNode eventBusLoad = previousOpcode(instruction);
                if (!(eventBusLoad instanceof VarInsnNode variable)
                        || variable.getOpcode() != Opcodes.ALOAD) {
                    throw new IllegalStateException(
                            "Could not identify the IEventBus argument for LinearBearingClient.registerClient");
                }

                AbstractInsnNode continueAt = instruction.getNext();
                constructor.instructions.remove(eventBusLoad);
                constructor.instructions.remove(instruction);

                removed++;
                instruction = continueAt;
                continue;
            }

            instruction = next;
        }

        return removed;
    }

    private static boolean referencesOwner(InvokeDynamicInsnNode invokedynamic, String owner) {
        for (Object argument : invokedynamic.bsmArgs) {
            if (argument instanceof Handle handle && owner.equals(handle.getOwner())) {
                return true;
            }
        }
        return false;
    }

    private static AbstractInsnNode previousOpcode(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction.getPrevious();
        while (current != null && current.getOpcode() < 0) {
            current = current.getPrevious();
        }
        return current;
    }

    private static AbstractInsnNode nextOpcode(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction.getNext();
        while (current != null && current.getOpcode() < 0) {
            current = current.getNext();
        }
        return current;
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo) {
    }
}
