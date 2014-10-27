#! /usr/bin/gnuplot
reset
set terminal png

#write your output base dir path
BASE_DIR=""

set output "../plots/producePerformance.png"

set logscale x
set logscale y

set xlabel "time in ms"
set ylabel "records num"

set title "single thread produce performance test"

set key reverse Left outside
set grid

set style data linespoints

set yrange [10000:1000000]
set xrange [1000:100000]

plot "/tmp/single_thread_produce_one_by_one.data" using 1:2 title "client",\
	 "/tmp/single_thread_original_produce_one_by_one.data" using 1:2 title "original";