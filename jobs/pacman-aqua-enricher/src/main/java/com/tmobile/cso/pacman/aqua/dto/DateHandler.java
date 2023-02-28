package com.tmobile.cso.pacman.aqua.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHandler extends StdDeserializer<Date> {

  public DateHandler()
  {
    this(null);
  }
  public DateHandler(Class<?> clazz){
    super(clazz);
  }
  @Override
  public Date deserialize(JsonParser jsonParser,
                          DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException {
    String date = jsonParser.getText();
    try{
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      return sdf.parse(date);
      }
    catch (ParseException e){
      return null;
    }
  }
}
