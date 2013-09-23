package com.example.testing123_9_15_2013;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FirstActivity extends Activity
{
  public ArrayList<String> undoStack = new ArrayList<String>();
  public ArrayList<String> redoStack = new ArrayList<String>();
  
  EditText mEditText; //test
  
  @Override
  protected void onCreate(Bundle savedInstanceState)  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_first);
    
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
    
    
    Button firstButton = (Button) findViewById(R.id.myButton);
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
