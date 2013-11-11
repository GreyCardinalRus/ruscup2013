import model.*;

import java.util.Random;

public final class MyStrategy implements Strategy {
	private final Random random = new Random();
	static Bonus  moveToBonus = null;
	@Override
	public void move(Trooper self, World world, Game game, Move move) {
		if (self.getActionPoints() < 1) {// game.getStandingMoveCost()) {
			 moveToBonus = null;
			return;
		}
		Trooper[] trooreps = world.getTroopers();
		Player[] players = world.getPlayers();
		Bonus[] bonuses = world.getBonuses();

		boolean haveVisibleenemys = false;

		
		System.out.println("-=Why I am = " + self.getType()+" X="+self.getX()+" Y="+self.getY() + " : "
				+ self.getActionPoints());
		if( moveToBonus == null) 
		for (int i = 0; i < bonuses.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())
			Bonus  foundBonus = null;
			// System.out.println(bonuses[i].getType());
			if (bonuses[i].getType() == bonuses[i].getType().FIELD_RATION
					&& !self.isHoldingFieldRation()) {
				foundBonus = bonuses[i];
			} else if (bonuses[i].getType() == bonuses[i].getType().GRENADE
					&& !self.isHoldingGrenade()) {
				foundBonus = bonuses[i];
			} else if (bonuses[i].getType() == bonuses[i].getType().MEDIKIT
					&& !self.isHoldingMedikit()) {
				foundBonus = bonuses[i];
			}
            if(foundBonus!=null && moveToBonus == null) moveToBonus= foundBonus;
            else if(foundBonus==null) ;
            else if(self.getDistanceTo(moveToBonus)>self.getDistanceTo(foundBonus)) moveToBonus= foundBonus;
		}
	//	if (!(moveToBonus == null))
	//		System.out.println("Bonus = " + moveToBonus.getType());

		// System.out.println("	-=Svoy");
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (trooreps[i].isTeammate() && trooreps[i].getId() != self.getId()) {
				// System.out.println(trooreps[i].getType());
			}

		}

		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (!trooreps[i].isTeammate()) {
				haveVisibleenemys = true;
				// System.out.println(trooreps[i].getType());
			}

		}
		if (haveVisibleenemys)
			System.out.println("	-=Cysie");
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (!trooreps[i].isTeammate()) {
				// haveVisibleenemys =true;
				System.out.println(trooreps[i].getType());
			}
		}
		if (haveVisibleenemys) {
			move.setAction(ActionType.MOVE);

			if (random.nextBoolean()) {
				move.setDirection(random.nextBoolean() ? Direction.WEST
						: Direction.EAST);
			} else {
				move.setDirection(random.nextBoolean() ? Direction.SOUTH
						: Direction.NORTH);
			}
			// for (int pid = 0; pid < players.length; pid++) {
			// System.out.println("Player: "+players[pid].getName()+" ");
			// for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())
			// {
			// System.out.println(trooreps[i].getType()+trooreps[i].isTeammate());}
			//
			// }
			// }
		} else // freestyle
		{
			move.setAction(ActionType.MOVE);
			if (!(moveToBonus == null))
			{
				//System.out.println("Self = " + moveToBonus.getType()+"="+moveToBonus.getX()+" Y="+moveToBonus.getY());
				System.out.println("-=!=- Bonus : " + moveToBonus.getType()+" X="+moveToBonus.getX()+" Y="+moveToBonus.getY()+" id="+moveToBonus.getId());
				if(moveToBonus.getX()==self.getX())
				{
					move.setDirection(moveToBonus.getY()<self.getY() ? Direction.NORTH
							: Direction.SOUTH	);
				}
				else if(moveToBonus.getY()==self.getY()) moveToBonus = null;
				else
				{
					move.setDirection(moveToBonus.getX()<self.getX() ? Direction.WEST
							: Direction.EAST
							);	
				}
				System.out.println("-=!=- Direction : " +move.getDirection());
				//move.setX(moveToBonus.getX());
				//move.setY(moveToBonus.getY());
			
			}
		// get location

		}
	}
}
