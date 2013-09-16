package com.example.testing123_9_15_2013;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FirstActivity extends Activity
{

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_first);
    
    Button firstButton = (Button) findViewById(R.id.myButton);
    firstButton.setOnClickListener(new OnClickListener() {
    
    EditText myTextHandler = (EditText) findViewById(R.id.textBox);
        
      @Override
      public void onClick(View arg0) {
 
//      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mkyong.com"));
//      startActivity(browserIntent);
        myTextHandler.setText("Yo bitch I changed yo text");
        
 
      }
 
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.first, menu);
    return true;
  }

}
