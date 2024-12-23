import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateLargeJsonWithFastJson {

    public static void main(String[] args) throws IOException {
        // 目标文件
        FileWriter fileWriter = new FileWriter("1k_large_data_fastjson.json");

        // 创建JSON数组，用于存储所有数据
        JSONArray dataArray = new JSONArray();

        // 生成随机数据
        Random random = new Random();

        // 循环生成数据
        long sizeTarget = 1000;
        long currentSize = 0;

        while (currentSize < sizeTarget) {
            // 创建一个JSON对象，每个用户的数据
            JSONObject dataNode = new JSONObject();

            // 添加各种数据类型到每个对象
            dataNode.put("name", "User" + random.nextInt(1000000));
            dataNode.put("age", random.nextInt(100));
            dataNode.put("active", random.nextBoolean());
            dataNode.put("balance", random.nextDouble() * 10000);
            dataNode.put("description", "This is a sample description for user " + random.nextInt(1000000));

            // 创建一个嵌套的JSON对象（地址）
            JSONObject addressNode = new JSONObject();
            addressNode.put("street", "Street " + random.nextInt(1000));
            addressNode.put("city", "City " + random.nextInt(1000));
            addressNode.put("zipcode", "ZIP" + random.nextInt(10000));
            dataNode.put("address", addressNode);

            // 创建一个JSON数组（电话号码）
            JSONArray phoneNumbersNode = new JSONArray();
            phoneNumbersNode.add("123-456-" + random.nextInt(1000));
            phoneNumbersNode.add("789-012-" + random.nextInt(1000));
            dataNode.put("phoneNumbers", phoneNumbersNode);

            // 将这个对象添加到JSON数组中
            dataArray.add(dataNode);

            // 计算当前JSON数组的大小
            String jsonData = dataArray.toJSONString();
            currentSize = jsonData.getBytes().length;
        }

        // 创建根节点对象
        JSONObject rootNode = new JSONObject();
        rootNode.put("users", dataArray);

        // 将整个JSON数据写入文件
        fileWriter.write(rootNode.toJSONString());

        // 关闭文件写入流
        fileWriter.close();

        System.out.println("50MB JSON 文件已生成: 500k_large_data_fastjson.json");
    }
}
