package com.example.testing123_9_15_2013;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import edu.umich.imlc.collabrify.client.CollabrifyClient;
import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;

public class FirstActivity extends Activity
{
  public final String TAG = "wewrite";
  public ArrayList<String> undoStack = new ArrayList<String>();
  public ArrayList<String> redoStack = new ArrayList<String>();
  public boolean changedByButton = false;
  public Timer refreshTimer = new Timer("refreshTimer",true);
  public String currentRevision = "";
  public boolean expectedCursor = false;
  public int cursorPosition = 0;
  public Button connectButton;
  public Button getSessionButton;
  public Button leaveSessionButton;
  public CheckBox withBaseFile;
  public CollabrifyClient myClient;
  public ArrayList<String> tags = new ArrayList<String>();
  public String sessionName;
  public ByteArrayInputStream baseFileBuffer;
  public long sessionId;
  
  EditText mEditText; //test
  
  
  
  
  
  
  // --------------------------------- from Evan Noon and Quentin Long, still reconciling them ----------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_first);

    withBaseFile = (CheckBox) findViewById(R.id.withBaseFileCheckBox);
    connectButton = (Button) findViewById(R.id.ConnectButton);
    getSessionButton = (Button) findViewById(R.id.getSessionButton);
    leaveSessionButton = (Button) findViewById(R.id.LeaveSessionButton);

      boolean getLatestEvent = false;

      tags.add("sample");

      // Instantiate client object
      try
      {
        myClient = new CollabrifyClient(this, "user email", "user display name",
            "441fall2013@umich.edu", "XY3721425NoScOpE", getLatestEvent,
            new WeWriteCollabrifyAdapter(this));
      }
      catch( CollabrifyException e )
      {
        e.printStackTrace();
      }


      connectButton.setOnClickListener(new ConnectListener(this));
      getSessionButton.setOnClickListener(new JoinListener(this)); 
      leaveSessionButton.setOnClickListener(new LeaveListener(this)); 

    EditTextCursorWatcher mText = (EditTextCursorWatcher) this
        .findViewById(R.id.editText);
    mText.setMainActivity(this);
    startListener();
    refreshTimer.scheduleAtFixedRate(new refreshTask(), 1000, 1000);
    // refreshTimer.schedule(new mockRecv(), 12000);
    // refreshTimer.schedule(new mockRecv2(), 11100);
    // refreshTimer.schedule(new mockRecv3(), 10000);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
    case R.id.action_undo:
      if (undoStack.empty()) {
        Log.d("undo", "The stack is empty");
      } else {
        ChangeEvent newtxn = undoStack.pop();
        runTxn(newtxn, false);
        Log.d("undo", "Setting to");
      }
      return true;
    case R.id.action_redo:
      // refreshTimer.schedule(new mockRecv2(), 1000);
      if (redoStack.empty()) {
        Log.d("redo", "Redo stack empty");

      } else {
        CharSequence newText = redoStack.pop();
        EditTextCursorWatcher mText = (EditTextCursorWatcher) this
            .findViewById(R.id.editText);
        changedByButton = true;
        // undoStack.push(mText.getText());
        mText.setText(newText);
        Log.d("redo", "Setting to: " + newText);
      }
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  public void startListener() {
    EditTextCursorWatcher mText = (EditTextCursorWatcher) this
        .findViewById(R.id.editText);
  }

  public class ChangeEvent {
    /*
     * The changes to a string. Keep the first startPos chars and the last
     * endPos chars, replace middle with newData
     */
    public int startPos;
    public int endPos;
    public String newData;
  }

  public ChangeEvent calcDiff(String origString, String newStr) {
    /* Calculate the ChangeEvent for two strings */
    int index1 = origString.length();
    for (int x = 0; x < origString.length(); x++) {
      try {
        if (origString.charAt(x) != newStr.charAt(x)) {
          index1 = x;
          break;
        }
      } catch (IndexOutOfBoundsException e) {
        index1 = x;
        break;
      }
    }
    int index2 = origString.length();
    String reverseOrig = new StringBuilder(origString).reverse().toString();
    String reverseNew = new StringBuilder(newStr).reverse().toString();
    for (int x = 0; x < reverseOrig.length(); x++) {
      try {
        if (reverseOrig.charAt(x) != reverseNew.charAt(x)) {
          index2 = x;
          break;
        }
      } catch (IndexOutOfBoundsException e) {
        index2 = x;
        break;
      }
    }
    if (index1 == origString.length() && index2 == index1 && index1 != 0) {
      return null;
    }
    ChangeEvent returnChange = new ChangeEvent();
    returnChange.startPos = index1;
    returnChange.endPos = origString.length() - index2;
    try {
      returnChange.newData = newStr.substring(index1, newStr.length()
          - index2);
    } catch (IndexOutOfBoundsException e) {
      Log.d("index1", String.valueOf(index1));
      Log.d("index2", String.valueOf(index2));
      Log.d("newstr", newStr);
      returnChange.newData = "";
    }
    return returnChange;
  }

  public void createTxn() {
    // Create a ChangeEvent from currentRevision diff and broadcasts it
    final EditTextCursorWatcher mText = (EditTextCursorWatcher) MainActivity.this
        .findViewById(R.id.editText);
    CharSequence curText = mText.getText();
    final ChangeEvent txn = calcDiff(MainActivity.currentRevision,
        curText.toString());
    if (txn == null) {
      Log.d("null", " txn");
      return;
    }

    // Save the reverse of the event
    ChangeEvent undotxn = calcDiff(curText.toString(),
        MainActivity.currentRevision);
    MainActivity.undoStack.push(undotxn);
    final String curRev = MainActivity.currentRevision;
    MainActivity.cursorPosition = mText.getSelectionEnd();

    // Create the protobuffed txn
    Event.ChangeEvent eventTxn = Event.ChangeEvent.newBuilder()
      .setStart(txn.startPos)
      .setEnd(txn.endPos)
      .setNewdata(txn.newData)
      .build();
    byte[] txnBytes = eventTxn.toByteArray(); 

    // Broadcast 
    int subId;
    try {
      subId = this.myClient.broadcast(txnBytes, "msg");
    }
    catch (CollabrifyException e) {
      // TODO save unsent transactions, or reset to last state
      // maybe check for active connection at beginning of this fnct
      e.printStackTrace();
    }


    runOnUiThread(new Runnable() {
      public void run() {
        MainActivity.expectedCursor = true;
        if (curRev == null || curRev == "") {
          mText.setText("");
        } else {
          mText.setText(curRev);
        }

        // Log change
        String logStr = txn.newData;
        if (logStr.length() == 0) {
          logStr = "(delete)";
        }
        Log.d(String.valueOf(txn.startPos) + " "
            + String.valueOf(txn.endPos), logStr);

        // TODO: call runTxn here so our txn doesnt disappear
        // Then check to make sure its the next recvd event
        //runTxn(txn, true);
      }
    });

  }

  public void getTxn(Event.ChangeEvent serializedTxn) {
    // Deserialize event and run it
    MainActivity.ChangeEvent txn = new MainActivity.ChangeEvent();
    txn.startPos = serializedTxn.getStart();
    txn.endPos = serializedTxn.getEnd();
    txn.newData = serializedTxn.getNewdata();
    this.runTxn(txn, false);
  }

  public String applyTxn(ChangeEvent txn, String curText) {
    // Returns a string after txn is applied to curText
    String newText = "";
    String endText = "";
    try {
      if (txn.startPos != 0) {
        newText += curText.subSequence(0, txn.startPos);
      }
      if (txn.endPos != curText.length()) {
        endText += curText.subSequence(txn.endPos, curText.length());
      }
    } catch (IndexOutOfBoundsException e) {
      Log.d("ERROR", String.format("Invalid txn %d %d", txn.startPos,
          txn.endPos));
    }
    newText += txn.newData + endText;
    return newText;
  }

  public void runTxn(ChangeEvent txn, boolean localTxn) {
    // Applies the ChangeEvent and updates UI, current revision
    Log.d(String.valueOf(txn.startPos) + " " + String.valueOf(txn.endPos),
        "Txn: " + txn.newData);

    final EditTextCursorWatcher mText = (EditTextCursorWatcher) MainActivity.this
        .findViewById(R.id.editText);
    CharSequence curText = mText.getText();

    int curLen = MainActivity.currentRevision.length();
    int txnDelta = txn.newData.length() - (txn.endPos - txn.startPos);

    // Persist the unsent state through transaction
    ChangeEvent unsaved = null;
    if (!localTxn) {
      unsaved = calcDiff(MainActivity.currentRevision, curText.toString());
      if (unsaved != null) {
        if (txn.startPos > unsaved.endPos) {
          // Incoming TXN is completely past unsaved TXN, do nothing
        } else if (txn.endPos < unsaved.startPos) {
          // Incoming TXN is completely before unsaved TXN, shift ours
          // right
          unsaved.startPos += txnDelta;
          unsaved.endPos += txnDelta;
        } else if (unsaved.startPos <= txn.startPos) {
          // Txns overlap, change our endpos to before txn
          unsaved.endPos = txn.startPos;
        } else {
          // Txns overlap, change our start to after txn
          unsaved.startPos = txn.endPos + txnDelta;
          if (unsaved.endPos < txn.endPos) {
            unsaved.endPos = txn.endPos + txnDelta;
          }
        }
      }
    }

    // Update current revision
    String newText = applyTxn(txn, MainActivity.currentRevision);
    MainActivity.currentRevision = newText;

    // Update cursor position
    int cursorpos = mText.getSelectionEnd();
    if (localTxn) {
      cursorpos = MainActivity.cursorPosition;
    } else {
      if (txn.endPos <= cursorpos) {
        cursorpos += txnDelta;
      } else if (txn.startPos <= cursorpos) {
        cursorpos = txn.startPos;
      }
    }

    // Apply the unsaved changes back to the text
    if (unsaved != null) {
      newText = applyTxn(unsaved, newText);
      int unsavedSize = unsaved.newData.length()
          - (unsaved.endPos - unsaved.startPos);
      cursorpos = unsaved.endPos + unsavedSize;
    }

    final String finalText = newText;
    final int finalPos = cursorpos;
    runOnUiThread(new Runnable() {
      // Required to update UI from refresh daemon
      public void run() {
        MainActivity.expectedCursor = true;
        mText.setText(finalText);
        MainActivity.expectedCursor = true;
        mText.setSelection(finalPos);
      }
    });
  }

  class mockRecv extends TimerTask {
    public void run() {
      int textLen = MainActivity.currentRevision.length();
      final ChangeEvent txn = new ChangeEvent();
      txn.startPos = 0;
      txn.endPos = 0;
      txn.newData = "<beginning>";
      runTxn(txn, false);
    }
  }

  class mockRecv2 extends TimerTask {
    public void run() {
      int textLen = MainActivity.currentRevision.length();
      final ChangeEvent txn = new ChangeEvent();
      txn.startPos = textLen / 2;
      txn.endPos = textLen / 2;
      txn.newData = "<middle>";
      runTxn(txn, false);
    }
  }

  class mockRecv3 extends TimerTask {
    public void run() {
      int textLen = MainActivity.currentRevision.length();
      final ChangeEvent txn = new ChangeEvent();
      txn.startPos = textLen;
      txn.endPos = textLen;
      txn.newData = "<end>";
      runTxn(txn, false);
    }
  }

  class refreshTask extends TimerTask {
    public CharSequence lastText = "";
    public Boolean stillChanging = false;

    public void run() {
      EditTextCursorWatcher mText = (EditTextCursorWatcher) MainActivity.this
          .findViewById(R.id.editText);
      CharSequence curText = mText.getText();
      if (!curText.toString().equals(MainActivity.currentRevision)) {
        if (!curText.toString().equals(lastText.toString())) {
          lastText = curText.toString();
        } else {
          // text has stopped changing, apply txn
          createTxn();
        }
      }
    }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  /* ----------------------------------- OUR ORIGINAL CODE ------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState)  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_first);
    
    withBaseFile = (CheckBox) findViewById(R.id.withBaseFileCheckBox);
    connectButton = (Button) findViewById(R.id.ConnectButton);
    getSessionButton = (Button) findViewById(R.id.getSessionButton);
    leaveSessionButton = (Button) findViewById(R.id.LeaveSessionButton);
    
    boolean getLatestEvent = false;
    
    mEditText = (EditText) findViewById(R.id.textBox);
    
    mEditText.addTextChangedListener(new TextWatcher() {
      //private int time;
      //private String oldString;
      //Log.d ("pizza")
      
      public void onTextChanged(CharSequence s, int start, int before, int count) 
      {
        // Nothing
      };
      
      @Override
      public void afterTextChanged(Editable s)
      {
        Log.d("my app", s.toString());
        //send events to server, etc.
      }
      
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
          int after)
      {
        // TODO Auto-generated method stub
        
      }
    });
    
    
    Button firstButton = (Button) findViewById(R.id.undoButton);
    firstButton.setOnClickListener(new OnClickListener() {
        
      @Override
      public void onClick(View arg0) {
 
//      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mkyong.com"));
//      startActivity(browserIntent);
        mEditText.setText("Yo bitch I changed yo text");
        
 
      }
    });
    
    //myListeners();
  }
  
  //public void myListeners() {
      
  //}


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.first, menu);
    return true;
  }

}
*/
  
  
  
  
  