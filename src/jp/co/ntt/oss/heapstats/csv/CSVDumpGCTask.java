/*
 * Copyright (C) 2014 Yasumasa Suenaga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package jp.co.ntt.oss.heapstats.csv;

import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.LongAdder;
import javafx.concurrent.Task;
import jp.co.ntt.oss.heapstats.container.SnapShotHeader;

/**
 * CSV writer class for GC statistics and heap histogram.
 */
public class CSVDumpGCTask extends Task<Void>{
    
    /** Save the file name. */
    private final File csvFile;
    
    /** SnapShot header to dump */
    private final List<SnapShotHeader> headers;

    /**
     * Constructor of CSVDumpGC.
     *
     * @param csvFile File name of csv file to dump.
     * @param target List of SnapShotHeader to dump.
     */
    public CSVDumpGCTask(File csvFile, List<SnapShotHeader> target) {
        this.csvFile = csvFile;
        this.headers = target;
    }

    @Override
    protected Void call() throws Exception {
        
        try(PrintWriter writer = new PrintWriter(csvFile)){
            
            /* Write CSV header */
            writer.print("SnapShot Date,");
            writer.print("Full GC Count,");
            writer.print("Young GC Count,");
            writer.print("New Heap Usage,");
            writer.print("Old Heap Usage,");
            writer.print("Total Heap Size,");
            writer.print("GC Cause,");
            writer.print("GC Time,");
            writer.print("Metaspace Usage,");
            writer.print("Metaspace Capacity");
            writer.println();
            
            /* Dump data */
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS");
            LongAdder progress = new LongAdder();
            headers.stream()
                   .forEachOrdered(h -> {
                                           StringJoiner joiner = new StringJoiner(",");
                                           joiner.add(formatter.format(h.getSnapShotDate()))
                                                 .add(Long.toString(h.getFullCount()))
                                                 .add(Long.toString(h.getYngCount()))
                                                 .add(Long.toString(h.getNewHeap()))
                                                 .add(Long.toString(h.getOldHeap()))
                                                 .add(Long.toString(h.getTotalCapacity()))
                                                 .add(h.getGcCause())
                                                 .add(h.getGcTime() == 0 ? "-" : Long.toString(h.getGcTime()))
                                                 .add(h.getMetaspaceUsage() == 0 ? "-" : Long.toString(h.getMetaspaceUsage()))
                                                 .add(h.getMetaspaceCapacity() == 0 ? "-" : Long.toString(h.getMetaspaceCapacity()));
                                           writer.println(joiner.toString());
                                           
                                           progress.increment();
                                           updateProgress(progress.intValue(), headers.size());
                                        });
            
        }
        
        return null;        
    }

}
