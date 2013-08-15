package com.example.icdispatchdemo;

import java.util.Arrays;

import ICDispatch.ICBlock;
import ICDispatch.ICDispatch;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
    private int tasksRun;

    long[] runtime;
    long[] queuetime;
    private long mStartTime;
    private ListView mListView;
    private Long[] mProgress;
    private long[] mWait;
    private boolean[] mDone;
    private ProgressAdapter mAdapter;

    private static int MAX = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mListView = (ListView) findViewById(R.id.listView1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void startTest(View v) {
        int tasks = Integer.valueOf(((EditText) findViewById(R.id.editText1)).getText().toString());
        EditText edt = (EditText) findViewById(R.id.iterations);
        if (edt.getText().toString().length() > 0) {
            MAX = Integer.parseInt(edt.getText().toString());
        }
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);
        int test = rg.getCheckedRadioButtonId();

        this.mProgress = new Long[tasks];
        this.mDone = new boolean[tasks];
        this.mWait = new long[tasks];
        Arrays.fill(mWait, Long.MAX_VALUE);
        Arrays.fill(mProgress, (long)0);
        this.mAdapter = new ProgressAdapter(this, 0, mProgress,mDone, mWait);
        this.mListView.setAdapter(mAdapter);

        this.queuetime = new long[tasks];
        this.runtime = new long[tasks];
        this.mStartTime = System.currentTimeMillis();
        switch (test) {
        case R.id.radio0:
            startSeqTest(tasks);
            break;
        case R.id.radio1:
            startConcTest(tasks);
            break;
        case R.id.radio2:
            startPrioTest(tasks);
            break;
        }
    }

    private void startPrioTest(int tasks) {
        for (int i = 0; i < tasks; i++) {
            App.executeOn(i % 3, getBlock(i));
        }

    }

    private void startConcTest(int tasks) {
        for (int i = 0; i < tasks; i++) {
            App.executeOn(ICDispatch.CONCURRENT, getBlock(i));
        }

    }

    private void startSeqTest(int tasks) {
        for (int i = 0; i < tasks; i++) {
            App.executeOn(ICDispatch.NORMAL, getBlock(i));
        }
    }

    private ICBlock getBlock(final int index) {
        return new ICBlock() {
            private long mEnqueueTime = System.currentTimeMillis();
            private long mWaitTime;
            private long mStartTime;
            private long mRunTime;
           

            @Override
            public void run() {
                this.mStartTime = System.currentTimeMillis();
                this.mWaitTime = System.currentTimeMillis() - mEnqueueTime;
                App.executeOn(ICDispatch.MAIN, new ICBlock() {

                    @Override
                    public void run() {
                        mWait[index] = mWaitTime;
                        mAdapter.notifyDataSetChanged();
                    }
                });
                for (int i = 0; i < MAX; i++) {
                        mProgress[index] =  System.currentTimeMillis()-mStartTime ;
                }
                mDone[index] = true;
                this.mRunTime = System.currentTimeMillis() - mStartTime;
                queuetime[index] = mWaitTime;
                runtime[index] = mRunTime;
                App.executeOn(ICDispatch.MAIN, new ICBlock() {

                    @Override
                    public void run() {
                        tasksRun++;
                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getCount() == tasksRun) {
                            tasksRun = 0;
                            Builder adb = new Builder(MainActivity.this);
                            long totalRuntime = 0;
                            long totalQueuetime = 0;
                            Arrays.sort(queuetime);
                            Arrays.sort(runtime);
                            for (int i = 0; i < queuetime.length; i++) {
                                totalRuntime += runtime[i];
                                totalQueuetime += queuetime[i];
                            }
                            long avgRuntime = totalRuntime / runtime.length;
                            long avgQueuetime = totalQueuetime / queuetime.length;
                            StringBuilder sb = new StringBuilder();
                            String ms = " ms\n";
                            long totalTime = (System.currentTimeMillis() - MainActivity.this.mStartTime);
                            sb.append("Total runtime: " + totalTime + ms);
                            sb.append("Avg executiontime (queue + run): " + totalTime
                                    / queuetime.length + ms);
                            sb.append("min queue time: " + queuetime[0] + ms);
                            sb.append("max queue time: " + queuetime[queuetime.length - 1] + ms);
                            sb.append("avg queue time: " + avgQueuetime + ms);

                            sb.append("min run time: " + runtime[0] + ms);
                            sb.append("max run time: " + runtime[queuetime.length - 1] + ms);
                            sb.append("avg run time: " + avgRuntime + ms);
                            adb.setMessage(sb.toString());
                            adb.show();

                        }
                    }
                });

                // TODO Auto-generated method stub

            }
        };
    }

}
