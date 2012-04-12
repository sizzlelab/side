package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.HashSet;

import android.os.Parcel;
import android.os.Parcelable;

public class TypeFilter implements Parcelable {

	final HashSet<Long> types = new HashSet<Long>();
	
	public TypeFilter(Object[] types) {
		for (Object t : types) {
			this.types.add((Long) t);
		}
	}
	
	public TypeFilter() {
	}

	public void add(Long type) {
		types.add(type);
	}
	
	public boolean has(Long type) {
		return types.contains(type);
	}

	@Override
	public boolean equals(Object o) {
		
		if (! (o instanceof TypeFilter)) {
			return false;
		}
		
		// test comparison of HashMaps via equals method
		final TypeFilter that = (TypeFilter) o;
		
		if (this.types.size() != that.types.size()) {
			return false;
		}
		
		
		for (Long type : types) {
			if (!that.types.contains(type)) {
				return false;
			}
		}
		return true; 
	}
	
	@Override
	public int hashCode() {
		
		int hash = 17;
		for (Long l : types) {
			hash = hash * 31 + (int) (l ^ (l >>> 32));
		}
		
		return hash;
	}
	
	@Override
	public String toString() {
		return types.toString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeArray(types.toArray());
	}

	public static final Parcelable.Creator<TypeFilter> CREATOR = new Parcelable.Creator<TypeFilter>() {

		@Override
		public TypeFilter createFromParcel(Parcel source) {
			return new TypeFilter(source.readArray(getClass().getClassLoader()));
		}

		@Override
		public TypeFilter[] newArray(int size) {
			return new TypeFilter[size];
		}
	};

	public void addAll(ArrayList<Long> types) {
		this.types.addAll(types);
	}
}
