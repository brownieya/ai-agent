package com.brownie.brownieaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 基于本地文件（Kryo 序列化）实现的 {@link ChatMemoryRepository}。
 *
 * <p>作用：负责"持久化存储层"，把每个会话（conversationId）的历史消息
 * 序列化到磁盘上的一个独立文件里。</p>
 *
 * <p>注意：在当前 Spring AI 1.x 中，记忆被拆成了两层：</p>
 * <ul>
 *   <li>{@link ChatMemoryRepository}：只管存取（找会话、查消息、保存、删除）；</li>
 *   <li>{@code MessageWindowChatMemory}：在存储层之上实现"滑动窗口"，
 *       即只保留最近 N 条消息——这正是旧版 {@code ChatMemory.get(id, lastN)}
 *       里的 {@code lastN} 参数现在被移到的地方。</li>
 * </ul>
 *
 * <p>用法示例：</p>
 * <pre>
 *   ChatMemory chatMemory = MessageWindowChatMemory.builder()
 *           .chatMemoryRepository(new FileBaseChatMemoryRepository("D:/chat-memory"))
 *           .maxMessages(20)   // 可选：等价于旧的 lastN
 *           .build();
 * </pre>
 */
public class FileBaseChatMemoryRepository implements ChatMemoryRepository {

    private static final String FILE_SUFFIX = ".kryo";

    private final String baseDir;

    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);

        // 设置实例化策略，便于反序列化无默认构造器的对象。
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 构造对象时，指定文件保存目录。
     */
    public FileBaseChatMemoryRepository(String dir) {
        this.baseDir = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 返回所有会话 ID。
     *
     * <p>旧版 {@code ChatMemory} 没有这个方法，它是 {@code ChatMemoryRepository} 新增的。</p>
     */
    @Override
    public List<String> findConversationIds() {
        File dir = new File(baseDir);
        String[] files = dir.list((d, name) -> name.endsWith(FILE_SUFFIX));
        if (files == null) {
            return List.of();
        }
        return Arrays.stream(files)
                .map(name -> name.substring(0, name.length() - FILE_SUFFIX.length()))
                .toList();
    }

    /**
     * 按会话 ID 返回全部历史消息。
     *
     * <p>旧版是 {@code get(String conversationId, int lastN)}，需要在这里截取"最近 N 条"；
     * 新版只剩 {@code findByConversationId(String conversationId)}，返回全部消息，
     * "截取最近 N 条"交给外层的 {@code MessageWindowChatMemory} 处理。</p>
     */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        File file = getConversationFile(conversationId);
        if (!file.exists()) {
            return List.of();
        }
        try (Input input = new Input(new FileInputStream(file))) {
            return kryo.readObject(input, ArrayList.class);
        }
        catch (IOException e) {
            throw new IllegalStateException("读取会话记忆失败: " + conversationId, e);
        }
    }

    /**
     * 保存某个会话的完整消息列表。
     *
     * <p>旧版是 {@code add(String conversationId, List<Message> messages)}，语义是"追加"；
     * 新版的 {@code saveAll} 语义是"覆盖保存整份列表"，
     * 因为"追加 + 截断窗口"的逻辑已经由 {@code MessageWindowChatMemory} 完成。</p>
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        }
        catch (IOException e) {
            throw new IllegalStateException("保存会话记忆失败: " + conversationId, e);
        }
    }

    /**
     * 删除某个会话的记忆文件。
     *
     * <p>旧版叫 {@code clear(String)}，新版改名为 {@code deleteByConversationId(String)}。</p>
     */
    @Override
    public void deleteByConversationId(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取当前会话的文件，每个会话单独保存一个文件。
     */
    private File getConversationFile(String conversationId) {
        return new File(baseDir, conversationId + FILE_SUFFIX);
    }
}
