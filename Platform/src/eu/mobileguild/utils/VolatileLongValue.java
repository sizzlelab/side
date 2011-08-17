/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.utils;

public class VolatileLongValue {
	
	private long expirationTime;
	public long lastUpdateTime = 0;
	public int value = 0;

	public VolatileLongValue(long expirationTime) {
		this.expirationTime = expirationTime;
	}
	
	public void start() {
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	public void reset() {
		this.lastUpdateTime = 0;
	}
	
	public void update(int value, long now) {
		this.lastUpdateTime = now;
		this.value = value;
	}
	
	public boolean hasValue(long now) {
		return lastUpdateTime != 0 && now - lastUpdateTime <= expirationTime;
	}
}
