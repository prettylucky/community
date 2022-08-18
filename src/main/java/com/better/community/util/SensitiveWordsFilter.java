package com.better.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 敏感词过滤器，用于过滤用户发出的帖子是否含有敏感词
 * 使用前缀树检测帖子中是否含有关键字
 * 用户随时都有可能发帖子，故把这个类交给spring管理，随服务器启动创建对象，
 * 并在@PostConstruct标注的init方法把敏感词对应的前缀树创建好。
 * @Date 2022/7/10
 */
@Component
public class SensitiveWordsFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordsFilter.class);

    //定义头指针
    private final TrieNode rootNode = new TrieNode();

    private static final String REPLACEMENT = "**";

    //@PostConstruct可以在当前类注入到容器 执行完构造方法后执行init方法进行一些初始化
    //在这里我们把关键字读取出来，并初始化完成敏感字对应的前缀树
    @PostConstruct
    public void init() {

        try (
            //读入敏感字文件 classes/sensitive-words
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            //转换为字节流（缓冲流效率更高）
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ) {
            String s = null;
            while ((s = br.readLine()) != null) {
                rootNode.addKeyWord(s);
            }
        } catch (IOException e) {
            logger.debug("读入敏感词文件发生错误！ msg:" + e.getMessage());
        }
    }

    //前缀树类 (内部类)
    private class TrieNode {
        //数据域
        private char data;
        //指针域（前缀树中子节点）
        private final Set<TrieNode> subTrieNodes = new HashSet<>();
        //记录子节点个数
        private int count = 0;
        //关键词结束标记
        private boolean isKeywordEnd = false;


        public char getData() {
            return data;
        }

        public void setData(char data) {
            this.data = data;
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public boolean addSubTrieNode(char data) {
            count++;
            TrieNode subTrieNode = new TrieNode();
            subTrieNode.setData(data);
            return subTrieNodes.add(subTrieNode);
        }
        public TrieNode getSubTrieNode(char data) {
            Iterator<TrieNode> iterator = subTrieNodes.iterator();
            while(iterator.hasNext()) {
                TrieNode trieNode = iterator.next();
                if (trieNode.getData() == data) {
                    return trieNode;
                }
            }
            return null;
        }
        //添加一个敏感关键字
        public void addKeyWord(String keyWord) {
            TrieNode temp = rootNode;
            for (int i = 0; i < keyWord.length(); i++) {
                char c = keyWord.charAt(i);
                TrieNode subTrieNode = temp.getSubTrieNode(c);
                //判断是否存在字符c对应的子节点，如果不存在则创建
                if(subTrieNode == null) {
                    temp.addSubTrieNode(c);
                }
                //指针指向子节点，继续循环
                temp = temp.getSubTrieNode(c);
                //当达到关键字末尾，则标记 isKeywordEnd 为 true
                if (i == keyWord.length() - 1) {
                    temp.setKeywordEnd(true);
                }
            }
        }


    }
    //测试用的方法
    public void print() {
        Iterator<TrieNode> iterator = rootNode.subTrieNodes.iterator();
        while (iterator.hasNext()) {
            TrieNode node = iterator.next();
            System.out.println(node.getData());
        }

    }
//    public String filter(String text) {
//        //存储过滤后的文本
//        StringBuilder sb = new StringBuilder(text);
//        //创建三个指针，一个TrieNode指针用于评定是否是敏感词
//        //两个指针指向字符串，一个用于遍历字符串，判断每个是否是否是敏感词的开头
//        //               另一个用于当第一个指针判定是敏感词的开头后，则判定这个字符后面的字符是否匹配前缀树中的敏感词
//        //不出现敏感词首个字符的情况下，begin和check是同时移动的，
//        //若出现敏感词begin停止移动，check向后进一步确认是否是敏感词
//        //  如果不是敏感词，begin继续向后寻找敏感字符，check继续与begin同步
//        //  如果是敏感词，begin保持不动，check继续向后判断
//        //      如果判断为空，则begin向后移动，check与begin同步
//        //      如果有敏感词结束标记，表示这是一个敏感词，则替换敏感词为***
//        TrieNode node = rootNode;
//        int begin = 0;
//        int check = 0;
//
//        while (begin < text.length() && check < text.length() - 1) {
//            //1.拿到check对应的字符
//            char c = text.charAt(check);
//
//            //2.判断字符是否是特殊符号，如果是特殊符号直接跳过
//            if (isSymbol(c) && !(node == rootNode)) {
//                check++;
//                c = text.charAt(check);
//            }
//
//            //3.拿到字符对应前缀树节点，（如果为空表示不存在）
//            node = node.getSubTrieNode(c);
//            //4.判断前缀树中是否有对应的字符
//
//            //如果不是敏感词字符
//            if (node == null) {
//                //前缀树节点归位
//                node = rootNode;
//                //begin继续移动，check跟随begin
//                check = begin ++;
//            } else
//            //如果是敏感词结尾
//            if(node.isKeywordEnd()) {
//                //替换敏感词
//                sb.replace(begin - 1, check + 2, REPLACEMENT);
//                //指针归位
//                check = begin++;
//                node = rootNode;
//            } else
//            //如果是敏感词，但不是敏感词结尾
//            {
//                check++;
//            }
//        }
//
//        return sb.toString();
//    }

    public String filter(String text) {
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == rootNode) {
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubTrieNode(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                }
                // 检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }
        }
        return sb.toString();

    }

    public boolean isSymbol(char c) {
        //不是英文和数字，且不是东亚文字（0x2E80 - 0x9FFF）
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}
