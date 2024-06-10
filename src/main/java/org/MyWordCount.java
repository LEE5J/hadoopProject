package org;

import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.service.ProtoTextInputFormat;

public class MyWordCount {
    public static class MyMapper extends Mapper<Object,Text, Text,IntWritable>{
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
        public void map(Object key, Text value, Context context) throws IOException,InterruptedException{
            String line = value.toString();
            String match = "[^\uAC00-\uD7A3xfea-zA-Z\\s]"; //특수문자 제거
            line = line.replaceAll(match, "");
            StringTokenizer st = new StringTokenizer(line);
            while(st.hasMoreTokens()){
                word.set(st.nextToken().toLowerCase());
                context.write(word,one);
            }
        }
    }
    public static class MyReduce extends Reducer<Text, IntWritable, Text,IntWritable>{
        private IntWritable sumWritable = new IntWritable();
        protected  void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException{
            int sum = 0;
            for(IntWritable val : values){
                sum += val.get();
            }
            sumWritable.set(sum);
            context.write(key,sumWritable);
        }
    }
    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf,"WordCount");
        job.setJarByClass(MyWordCount.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReduce.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(ProtoTextInputFormat.class);
//        job.setInputFormatClass(TextInputFormat.class);

        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}











