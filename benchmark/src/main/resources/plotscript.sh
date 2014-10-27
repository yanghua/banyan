#! /usr/bin/env gnuplot
reset
set terminal png

#write you image dir path
set output ""

#set logscale x
#set logscale y

set xlabel "time (ms)"
set ylabel "records num"

set title "single thread produce performance test "

set key reverse Left outside
set grid

set style data linespoints

set yrange [0:400000]
set xrange [0:60000]

plot "/tmp/single_thread_produce_one_by_one_size_0.5_KB.data" using 1:2 title "client-0.5KB",\
	 "/tmp/single_thread_original_produce_one_by_one_0.5_KB.data" using 1:2 title "original-0.5KB",\
	 "/tmp/single_thread_produce_one_by_one_size_1.0_KB.data" using 1:2 title "client-1KB",\
	 "/tmp/single_thread_original_produce_one_by_one_1.0_KB.data" using 1:2 title "original-1KB",\
	 "/tmp/single_thread_produce_one_by_one_size_5.0_KB.data" using 1:2 title "client-5KB",\
	 "/tmp/single_thread_original_produce_one_by_one_5.0_KB.data" using 1:2 title "original-5KB";