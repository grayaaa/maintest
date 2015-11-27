package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

/**
 * Created by qmgeng on 2015/5/13.
 */
public class Testhbase {
    public static void main(String[] args) throws IOException {

        // kerberos的配置文件的位置,windows下叫krb5.ini,linux下叫krb5.conf
        //System.setProperty("java.security.krb5.conf", "C:\\Windows\\krb5.ini");

        // win下的配置，防止出现winutil.exe的异常，hadoop.home.dir\bin目录下要有编译好的exe
        // 不配这个属性也没影响，就是会有个异常，不影响运行
        //System.setProperty("hadoop.home.dir", "D:\\hadoop-2.5.2");

        // 会自动加载hbase-site.xml
        Configuration conf = HBaseConfiguration.create();
        // conf.set("hbase.zookeeper.quorum", "hbase0.photo.163.org,hbase1.photo.163.org,hbase2.photo.163.org");
        // conf.set("hbase.zookeeper.property.clientPort", "2181");

        // 使用keytab登陆
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("weblog/dev@HADOOP.HZ.NETEASE.COM", "/home/weblog/weblog.keytab");

        // 定时调用更新kerberos（10小时过期），推荐用守护线程定期调用
        UserGroupInformation.getCurrentUser().reloginFromKeytab();

         HTablePool pool = new HTablePool(conf, 10);
         HTableInterface table = pool.getTable("datacube:kpidata");
         Put put = new Put(Bytes.toBytes("pro1_url1_20151101"));
         // 参数出分别：列族、列、值
         put.add(Bytes.toBytes("values"), Bytes.toBytes("pv"), Bytes.toBytes("1234567"));

         table.put(put);
         table.close();


        // 测试get
        HTable table2 = new HTable(conf, "datacube:kpidata");
        Get get = new Get("pro1_url1_20151101".getBytes());
        Result rs = table2.get(get);
        for (KeyValue kv : rs.raw()) {
            System.out.print(new String(kv.getRow()) + " ");
            System.out.print(new String(kv.getFamily()) + ":");
            System.out.print(new String(kv.getQualifier()) + " ");
            System.out.print(kv.getTimestamp() + " ");
            System.out.println(new String(kv.getValue()));
        }
        table2.close();

        // // 测试create
        // String newTableName = "sentry6";
        // HBaseAdmin admin = new HBaseAdmin(conf);
        // if (admin.tableExists(newTableName)) {
        // System.out.println("table " + newTableName + " already exists!");
        // } else {
        // HTableDescriptor tableDesc = new HTableDescriptor(newTableName);
        // tableDesc.addFamily(new HColumnDescriptor("cfamily"));
        //
        // admin.createTable(tableDesc);
        // System.out.println("create table " + newTableName + " ok.");
        // }
        // admin.close();
        //
        // // 测试put
        // table = new HTable(conf, newTableName);
        // Put put = new Put(Bytes.toBytes("row1"));
        // put.add(Bytes.toBytes("cfamily"), Bytes.toBytes("qualifier"), Bytes.toBytes("this.is.a.test.value"));
        // table.put(put);
        // System.out.println("insert recored ok.");
        // table.close();
    }

}
