package mx.ori.pronouns;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageType {
    private static final HashSet<Integer> index0 = Stream.of(0).collect(Collectors.toCollection(HashSet::new));
    private static final HashSet<Integer> index1 = Stream.of(1).collect(Collectors.toCollection(HashSet::new));
    private static final HashSet<Integer> index2 = Stream.of(2).collect(Collectors.toCollection(HashSet::new));
    private static final HashSet<Integer> index01 = Stream.of(0, 1).collect(Collectors.toCollection(HashSet::new));

    private static final HashMap<String, HashSet<Integer>> indicesMap = new HashMap<>() {{
        put("chat.type.admin",                                 index0);
        put("chat.type.admin",                                 index0);
        put("chat.type.advancement.challenge",                 index0);
        put("chat.type.advancement.goal",                      index0);
        put("chat.type.advancement.task",                      index0);
        put("chat.type.emote",                                 index0);
        put("chat.type.text",                                  index0);
        put("commands.advancement.grant.many.to.one.success",  index1);
        put("commands.advancement.grant.one.to.one.success",   index1);
        put("commands.advancement.revoke.many.to.one.success", index1);
        put("commands.advancement.revoke.one.to.one.success",  index1);
        put("commands.attribute.base_value.get.success",       index1);
        put("commands.attribute.base_value.set.success",       index1);
        put("commands.attribute.modifier.add.success",         index1);
        put("commands.attribute.modifier.remove.success",      index1);
        put("commands.attribute.modifier.value.get.success",   index1);
        put("commands.attribute.value.get.success",            index1);
        put("commands.clear.success.single",                   index1);
        put("commands.effect.give.success.single",             index1);
        put("commands.effect.clear.everything.success.single", index0);
        put("commands.effect.clear.specific.success.single",   index1);
        put("commands.enchant.success.single",                 index1);
        put("commands.experience.add.levels.success.single",   index1);
        put("commands.experience.add.points.success.single",   index1);
        put("commands.experience.query.levels",                index0);
        put("commands.experience.query.points",                index0);
        put("commands.experience.set.levels.success.single",   index1);
        put("commands.experience.set.points.success.single",   index1);
        put("commands.gamemode.success.other",                 index0);
        put("commands.give.success.single",                    index2);
        put("commands.kill.success.single",                    index0);
        put("commands.message.display.incoming",               index0);
        put("commands.message.display.outgoing",               index0);
        put("commands.teleport.success.entity.single",         index01);
        put("commands.teleport.success.entity.single",         index1);
        put("death.attack.outOfWorld",                         index0);
        put("multiplayer.player.joined",                       index0);
        put("multiplayer.player.left",                         index0);
    }};

    public @NotNull static Set<Integer> indexOf(String key) {
        var indices = indicesMap.get(key);
        return indices != null ? indices : new HashSet<>();
    }
}
