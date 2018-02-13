package smkra.weather.model;

import java.io.StringWriter;

public class Temperature {
	private Degree min;
	private Degree max;
	public Degree getMin() {
		return min;
	}
	public void setMin(Degree min) {
		this.min = min;
	}
	public Degree getMax() {
		return max;
	}
	public void setMax(Degree max) {
		this.max = max;
	}
	
	public String toString(){
		if (min != null || max != null){
			StringWriter writer = new StringWriter();
			if (min != null){
				writer.write("最低気温"+min.getCelsius()+"°C");
				if(max != null)
					writer.write(",");
				else
					writer.write("です。");
			}
			if (max != null){
				writer.write("最高気温"+max.getCelsius()+"°Cです。");
			}
			return writer.toString();
		} else return "";
	}
}
