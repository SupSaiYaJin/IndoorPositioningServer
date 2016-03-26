package com.saiya.tests;

import com.saiya.service.location.Algorithms;
import com.saiya.service.location.EuclideanDist;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate测试类
 */

public class HibernateTest {

    @Test
    public void doTest() {
        //真实位置
        List<Location> realLocation = new ArrayList<>();
        //计算位置
        List<Location> calLocation = new ArrayList<>();
        //场景名
        String sceneName = "家";
        //MAC地址信息,每个String为一个地点的所有MAC地址
        List<String> macs = new ArrayList<>();
        //RSSI信息,每个String为一个地点的所有RSSI
        List<String> rssis = new ArrayList<>();
        //欧氏距离信息,包含每个定位点的所有欧氏距离数据
        List<List<EuclideanDist>> euclideanDists = new ArrayList<>();
        //实测数据存放地点
        File file = new File("/Users/SaiYa/Documents/Code/Graduate/文档/数据/实测数据/明光楼9层.txt");
        //计算输出数据并输出
        //读取输入数据到内存
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split(" ");
                if (elements.length == 4) {
                    realLocation.add(new Location(Double.parseDouble(elements[0]),
                            Double.parseDouble(elements[1])));
                    macs.add(elements[2]);
                    rssis.add(elements[3]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //计算定位数据
        for (int i = 0; i < macs.size(); ++i) {
            List<EuclideanDist> temp =
                    Algorithms.getEuclideanDist(sceneName, macs.get(i), rssis.get(i));
            double[] res = Algorithms.calculateByWeight(temp);
            calLocation.add(new Location(res[0], res[1]));
            euclideanDists.add(temp);
        }
        File dis = new File("/Users/SaiYa/Documents/Code/Graduate/文档/数据/分析后数据/误差对比.csv");
        File eud = new File("/Users/SaiYa/Documents/Code/Graduate/文档/数据/分析后数据/欧氏距离.csv");
        try (FileWriter euWriter = new FileWriter(eud, true);
                FileWriter disWriter = new FileWriter(dis, true)) {
            for (int i = 0; i < realLocation.size(); ++i) {
                double realX = realLocation.get(i).x;
                double realY = realLocation.get(i).y;
                double calX = calLocation.get(i).x;
                double calY = calLocation.get(i).y;
                String location = "" + realX + "," + realY + "," + calX + "," + calY;
                disWriter.write(location + "\n");
                euWriter.write(location + "\n");
                for (EuclideanDist dist : euclideanDists.get(i)) {
                    euWriter.write(dist.getLocation_x() + "," + dist.getLocation_y() + ","
                            + dist.getEuclideanDist() + "\n");
                }
                euWriter.write("\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}

class Location {
    Location(double x, double y) {
        this.x = x;
        this.y = y;
    }
    double x;
    double y;
}
