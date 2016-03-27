package mapreduce.frequency.total;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.codehaus.jettison.json.JSONObject;

public class TotalJob extends Configured implements Tool {

	@Override
	public int run(String[] arg0) throws Exception {
		Job job = new Job(getConf());

		String separator = "=";

		final Configuration conf = job.getConfiguration();
		conf.set("mapred.textoutputformat.separator", separator); // Prior to
																	// Hadoop 2
																	// (YARN)
		conf.set("mapreduce.textoutputformat.separator", separator); // Hadoop
																		// v2+
																		// (YARN)
		conf.set("mapreduce.output.textoutputformat.separator", separator);
		conf.set("mapreduce.output.key.field.separator", separator);
		conf.set("mapred.textoutputformat.separatorText", separator); // ?

		job.setJarByClass(getClass());
		job.setJobName(getClass().getSimpleName());

		FileInputFormat.addInputPath(job, new Path("/home/mark/Desktop/Big_data/output/part-r-00000"));
		FileOutputFormat.setOutputPath(job, new Path("/home/mark/Desktop/Big_data/totals"));

		job.setMapperClass(TotalMapper.class);
		job.setCombinerClass(TotalReducer.class);
		job.setReducerClass(TotalReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);

		Date start = new Date();
		if (job.waitForCompletion(true) == true) {
			Date end = new Date();
			long elapsed = (end.getTime() - start.getTime());
			System.out.println(elapsed);

			return 0;
		} else {
			return 1;
		}
	}

	public static void main(String[] args) throws Exception {
		FileUtils.deleteDirectory(new File("/home/mark/Desktop/Big_data/totals"));
		TotalJob j = new TotalJob();
		int rc = ToolRunner.run(j, args);
		
		JSONObject data = new JSONObject();

		FileWriter writer = new FileWriter(new File("/home/mark/Desktop/Big_data/totals/part-r-00000.graph"));
		Properties p = new Properties();
		p.load(new FileInputStream(new File("/home/mark/Desktop/Big_data/totals/part-r-00000")));
		for (Entry<Object, Object> e : p.entrySet()) {
			JSONObject tmp = new JSONObject();
			tmp.put(e.getKey().toString(), e.getValue().toString());
			data.put(e.getKey().toString(), tmp);
			writer.append(e.getKey().toString() + " " + e.getValue().toString() + " ");
		}
		
		FileWriter jsonOutput = new FileWriter(new File("/home/mark/Desktop/Big_data/totals/part-r-00000.jgraph"));
		jsonOutput.write(data.toString());
		jsonOutput.close();

		writer.close();

		System.exit(rc);

	}

}