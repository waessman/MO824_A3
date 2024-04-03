package solutions;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Solution<E> extends ArrayList<E> {
	
	public Double cost = Double.POSITIVE_INFINITY;
	
	public Double weigth;
	
	public Solution() {
		super();
	}
	
	public Solution(Solution<E> sol) {
		super(sol);
		cost = sol.cost;
		weigth = sol.weigth;
	}

	@Override
	public String toString() {
		return "Solution: cost=[" + cost + "], peso=[" + weigth + "] size=[" + this.size() + "], elements=" + super.toString();
	}

}

