#! /usr/bin/env gnuplot
reset
set terminal png size 800,600

#write you image dir path
set output "/Users/yanghua/Documents/GitHub/messagebus/screenshots/benchmark/produce/singleThreadMultipleMsgBodySizeMBClientProduce.png"

#set logscale x
#set logscale y

set xlabel "time (ms)"
set ylabel "records num"

set title "single thread messagebus-client produce performance "

set key reverse Left outside
set grid
set autoscale

set style data linespoints

set yrange [0:500000]
set xrange [0:60000]

plot "/tmp/one_thread_produce_one_by_one_500_Byte.data" using 1:2 title "messagebus 0.5KB",\
	 "/tmp/one_thread_produce_one_by_one_1000_Byte.data" using 1:2 title "messagebus 1KB",\
	 "/tmp/one_thread_produce_one_by_one_2000_Byte.data" using 1:2 title "messagebus 2KB",\
	 "/tmp/one_thread_produce_one_by_one_3000_Byte.data" using 1:2 title "messagebus 3KB";