import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

/**
* This java program is the 3rd machine problem for our CMSC 125 (Operating Systems)
* Lab3 is a simulation of memory management, which allows the user to see how the 
* OS allocates memory for each process.
*
* @author Heidi Chiu
* @version 1.0
* @since 2017-10-15
*/

public class Lab3 {

	public static class Job {
		int id;
		int time;
		int size;
		boolean done;

		Job(int id, int time, int size) {
			this.id = id;
			this.time = time;
			this.size = size;
			this.done = false;
		}

		public void setDone(boolean done) {
			this.done = done;
		}

		public void decTime() {
			this.time--;
		}

		@Override
		public boolean equals(Object job) {
			return this.id == ((Job)job).id;
		}
	}

	public static class Memory {
		int id;
		int size;
		Job job;
		boolean isoccupied;

		Memory(int id, int size) {
			this.id = id;
			this.size = size;
			this.isoccupied = false;
			this.job = null;
		}

		public void setIsoccupied(boolean isoccupied) {
			this.isoccupied = isoccupied;
		}

		public void setJob(Job job) {
			this.job = job;
		}

		public Job getJob() {
			return this.job;
		}

		/**
		* This method is used to compare memory blocks from one another
		* @param m1, m2, the memory blocks to be compared
		* @return Whether m1 > m2.
		*/
		public static Comparator<Memory> MemoryComparator = new Comparator<Memory>() {
			public int compare(Memory m1, Memory m2) {
				int size1 = m1.size;
				int size2 = m2.size;

				return size1-size2;
			}
		};
	}

	/**
	* This method is used to display the list of jobs in the waiting queue
	* @param jobList This is the list of jobs
	* @return Nothing.
	*/
	public static void displayJobs(ArrayList jobList) {
		System.out.println("Job Stream #   |   Time   |   Job Size");
		System.out.println("--------------------------------------");
		for(int i = 0; i < jobList.size(); i++) {
			System.out.println("      " + ((Job)jobList.get(i)).id + "             " + 
				((Job)jobList.get(i)).time + "           " + ((Job)jobList.get(i)).size);
		}
	}

	/**
	* This method is used to display the list of memory blocks available
	* @param memoryList This is the list of memory blocks
	* @return Nothing.
	*/
	public static void displayMemory(ArrayList memoryList) {
		System.out.println("Memory Block   |   Size");
		System.out.println("-----------------------");
		for(int i = 0; i < memoryList.size(); i++) {
			System.out.println("      " + ((Memory)memoryList.get(i)).id 
				+ "            " + ((Memory)memoryList.get(i)).size);
		}
	}

	/**
	* This method is used to display the running status of the memory
	* @param memoryList This is the list of memory blocks
	* @return memused Which is the percent of the whole memory being used per ms.
	*/
	public static float displayRun(ArrayList memoryList) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		float percentfrag;
		float memused = 0;

		System.out.println("Memory Block   |   Job #   |   Time   |   Internal Fragmentation   |   %");
		System.out.println("------------------------------------------------------------------------------");
		String job = "";
		String time = "";
		String intfrag = "";
		String intfrag2 = "";

		int job_;
		int time_;
		int intfrag_;
		int intfrag2_;

		for(int i = 0; i < memoryList.size(); i++) {
			if(((Memory)memoryList.get(i)).job == null) {
				job = "";
				time = "";
				intfrag = "";
				intfrag2 = "";
			} else {
				job_ = ((Memory)memoryList.get(i)).job.id;
				time_ = ((Memory)memoryList.get(i)).job.time;
				intfrag_ = (((Memory)memoryList.get(i)).size) -
					((Job)((Memory)memoryList.get(i)).getJob()).size;

				job = Integer.toString(job_);
				time = Integer.toString(time_);
				intfrag = Integer.toString(intfrag_);
				percentfrag = ((float)(((Memory)memoryList.get(i)).size) -
					((float)((Job)((Memory)memoryList.get(i)).getJob()).size)) /
					(float)(((Memory)memoryList.get(i)).size) * 100;
				memused+=(float)((Job)((Memory)memoryList.get(i)).getJob()).size;
				intfrag2 = df.format(percentfrag) + '%';
			}
			System.out.println("      " + Integer.toString(((Memory)memoryList.get(i)).id) 
				+ "            " + job + "            " + time + "              " + 
				intfrag + "                  " + intfrag2);
		}	

		memused = (memused / 50000) * 100;
		System.out.println("\n" + memused + "% memory used");
		return memused;
	}

	/**
	* This method is used to display the status of the jobs after being processed
	* @param jobList This is the list of jobs
	* @return Nothing.
	*/
	public static void displayJobStatus(ArrayList jobList) {
		System.out.println("Job Stream #   |   Processed?");
		System.out.println("-----------------------------");
		for(int i = 0; i < jobList.size(); i++) {
			System.out.println("      " + Integer.toString(((Job)jobList.get(i)).id) + "             " + 
				((Job)jobList.get(i)).done);
		}
	}

	/**
	* This method is used to simulate the First-fit algorithm for memory allocation
	* Makes use of displayRun for each timer increment
	* Makes use of displayJobStatus before exiting
	* @param jobList This is the list of jobs
	* @param memoryList This is the list of memory blocks
	* @return Nothing.
	*/
	public static void firstFit(ArrayList jobList, ArrayList memoryList) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);

		boolean canrun = true;
		int timer = 0;
		int runningjobs;
		int undonejob = 0;
		float waitingtime = 0;
		float throughput = 0;
		float memused = 0;

		while(canrun) {
			runningjobs = 0;
			undonejob = 0;
			canrun = false;
			for(int i = 0; i < jobList.size(); i++) {
				if(((Job)jobList.get(i)).done == false) {
					for(int j = 0; j < memoryList.size(); j++) {
						if(!((Memory)memoryList.get(j)).isoccupied) {							if(((Memory)memoryList.get(j)).size > 
								((Job)jobList.get(i)).size) {
								((Memory)memoryList.get(j)).setJob(((Job)jobList.get(i)));
								((Memory)memoryList.get(j)).setIsoccupied(true);
								((Job)jobList.get(i)).setDone(true);
								waitingtime+=(float)timer;
								break;
							}
						} else {
							canrun = true;
						}
					}
				} 
			}

			for(int i = 0; i < jobList.size(); i++) {
				if(((Job)jobList.get(i)).done == false) {
					undonejob++;
				}
			}

			for(int j = 0; j < memoryList.size(); j++) {
				if(((Memory)memoryList.get(j)).isoccupied) {
					runningjobs++;
				}
			}

			throughput+=runningjobs;

			System.out.println("Timer: " + timer + '\n');
			memused += displayRun(memoryList);
			System.out.println(runningjobs + " job(s)/ms");
			System.out.println(undonejob + " job(s) waiting in queue");

			for(int i = 0; i < memoryList.size(); i++) {
				if(((Memory)memoryList.get(i)).isoccupied) {
					((Job)((Memory)memoryList.get(i)).getJob()).decTime();
					if(((Job)((Memory)memoryList.get(i)).getJob()).time == 0) {
						((Memory)memoryList.get(i)).setJob(null);
						((Memory)memoryList.get(i)).setIsoccupied(false);
					}
				}
			}

			try {
				Thread.sleep(500);
			} catch(InterruptedException ex) {
				//do nothing
			}
			System.out.println("\033[H\033[2J");
			timer++;
		}

		displayJobStatus(jobList);

		throughput = throughput/((float)timer);
		memused = memused / timer;
		waitingtime = waitingtime / ((float)jobList.size());
		System.out.println("\nAverage throughput: " + df.format(throughput) + " job(s)/ms");
		System.out.println("Average memory usage: " + df.format(memused) + "%");
		System.out.println("Average waiting time: " + df.format(waitingtime) + "ms");
		System.out.println();

		
		try {
			Thread.sleep(5000);
		} catch(InterruptedException ex) {
			//do nothing
		}
		System.out.println("\033[H\033[2J");
	}

	/**
	* This method is used to simulate the Worst-fit algorithm for memory allocation
	* Makes use of displayRun for each timer increment
	* Makes use of displayJobStatus before exiting
	* @param jobList This is the list of jobs
	* @param memoryList This is the list of memory blocks
	* @return Nothing.
	*/
	public static void worstFit(ArrayList jobList, ArrayList memoryList) {
		Collections.sort(memoryList, Memory.MemoryComparator.reversed());
		firstFit(jobList, memoryList);	
	}

	/**
	* This method is used to simulate the Best-fit algorithm for memory allocation
	* Makes use of displayRun for each timer increment
	* Makes use of displayJobStatus before exiting
	* @param jobList This is the list of jobs
	* @param memoryList This is the list of memory blocks
	* @return Nothing.
	*/
	public static void bestFit(ArrayList jobList, ArrayList memoryList) {
		Collections.sort(memoryList, Memory.MemoryComparator);
		firstFit(jobList, memoryList);
	}

	/**
	* This is the main method which makes use of firstFit, worstFit, and bestFit.
	* @param args Unused.
	* @return Nothing.
	*/
	public static void main(String[] args) {
		ArrayList jobList = new ArrayList<Job>();
		ArrayList jobList2 = new ArrayList<Job>();
		ArrayList memoryList = new ArrayList<Memory>();
		ArrayList memoryList2 = new ArrayList<Memory>();

		BufferedReader br = null;
		FileReader fr = null;
		StringTokenizer st;
		String line;

		boolean running = true;
		Scanner s;

		try {
			fr = new FileReader("joblist.txt");
			br = new BufferedReader(fr);

			int id;
			int time;
			int size;

			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line);

				id = Integer.parseInt(st.nextToken());
				time = Integer.parseInt(st.nextToken());
				size = Integer.parseInt(st.nextToken());

				jobList2.add(new Job(id, time, size));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}

		try {
			fr = new FileReader("memorylist.txt");
			br = new BufferedReader(fr);

			int id;
			int size;

			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line);

				id = Integer.parseInt(st.nextToken());
				size = Integer.parseInt(st.nextToken());

				memoryList2.add(new Memory(id, size));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		while(running) {

			jobList = new ArrayList<Job>();
			for(int i = 0; i < jobList2.size(); i++) {
				jobList.add(new Job(((Job)jobList2.get(i)).id, 
					((Job)jobList2.get(i)).time,
					((Job)jobList2.get(i)).size));
			}

			memoryList = new ArrayList<Memory>();
			for(int i = 0; i < memoryList2.size(); i++) {
				memoryList.add(new Memory(((Memory)memoryList2.get(i)).id, 
					((Memory)memoryList2.get(i)).size));
			}

			System.out.println("\033[H\033[2J");

			System.out.println("Choose: ");
			System.out.println("1. First-fit");
			System.out.println("2. Worst-fit");
			System.out.println("3. Best-fit\n");

			s = new Scanner(System.in);
			int option = s.nextInt();

			System.out.println();

			for(int i = 0; i < 5; i++) {
				System.out.println("Simulation starting in " + (5 - i) + " ... ");
				try {
					Thread.sleep(500);
				} catch(InterruptedException ex) {
					//do nothing
				}
			}

			System.out.println("\033[H\033[2J");

			switch(option) {
				case 1:
					firstFit(jobList, memoryList);
					break;
				case 2:
					worstFit(jobList, memoryList);
					break;
				case 3:
					bestFit(jobList, memoryList);
					break;
				default:
					break;
			}

			System.out.println("\033[H\033[2J");
			System.out.println();

			System.out.println("Continue? Y for yes, any character no");
			
			s = new Scanner(System.in);
			char cont = (char) s.next().charAt(0);

			switch(cont) {
				case 'y':
					running = true;
					break;
				case 'Y':
					running = true;
					break;
				default:
					running = false;
					break;
			}
			cont = ' ';
		}

	}
}