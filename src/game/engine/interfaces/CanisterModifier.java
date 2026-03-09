package game.engine.interfaces;
import game.engine.monsters.Monster;
public interface CanisterModifier{

	void Change_Energy(Monster shrek,int newEnergy);
}
class CanisterModifierClass implements CanisterModifier{
	@Override
	public void Change_Energy(Monster shrek,int newEnergy) {
		shrek.setEnergy(newEnergy);
	}
}