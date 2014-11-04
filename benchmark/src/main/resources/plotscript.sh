#! /usr/bin/env gnuplot
reset
set terminal png

#write you image dir path
set output "/Users/yanghua/Documents/GitHub/messagebus/screenshots/benchmark/consume/singleThreadClientVSOriginal.png"

#set logscale x
#set logscale y

set xlabel "time (ms)"
set ylabel "records num"

set title "single thread async consume performance test "

set key reverse Left outside
set grid
set autoscale

set style data linespoints

set yrange [0:500000]
set xrange [0:60000]

plot "/tmp/single_thread_consume_async_1.0_KB.data" using 1:2 title "client 1KB",\
	 "/tmp/single_thread_original_consume_async_1.0_KB.data" using 1:2 title "original 1KB",\
	 "/tmp/single_thread_consume_async_3.0_KB.data" using 1:2 title "client 3KB",\
	 "/tmp/single_thread_original_consume_async_3.0_KB.data" using 1:2 title "original 3KB",\
	 "/tmp/single_thread_consume_async_5.0_KB.data" using 1:2 title "client 5KB",\
	 "/tmp/single_thread_original_consume_async_5.0_KB.data" using 1:2 title "original 5KB";