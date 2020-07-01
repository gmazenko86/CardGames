/*
select dealerhands.hashid as hashid, dealerhands.total as dtot,
dealerhands.attribute as dattrib, dealerhands.result as dresult,
dealerhands.card1 as dcard1, dealerhands.card2 as dcard2,
dealerhands.card3 as dcard3, dealerhands.card4 as dcard4,
dealerhands.card5 as dcard5, dealerhands.card6 as dcard6,
dealerhands.card7 as dcard7, dealerhands.card8 as dcard8,
playerhands.total as ptot,
playerhands.attribute as pattrib, playerhands.result as presult,
playerhands.card1 as pcard1, playerhands.card2 as pcard2,
playerhands.card3 as pcard3, playerhands.card4 as pcard4,
playerhands.card5 as pcard5, playerhands.card6 as pcard6,
playerhands.card7 as pcard7, playerhands.card8 as pcard8
into newtable
from dealerhands left join playerhands using(hashid);
*/

--select count(presult) from newtable;
--select count(presult) from newtable where presult = 'WIN';
--select count(presult) from newtable where presult = 'PUSH';
--select count(presult) from newtable where presult = 'LOSE';

--select * from dealerhands left join playerhands using(hashid) order by hashid;

--select count(distinct hashid) as dealer, count(hashid) as player from playerhands;

--select count(hashid) from d1p1_1;
--select count(hashid) from d2p1_1;
--select count(hashid) from d3p1_1;

--drop table d1p1_1;
--drop table d2p1_1;
--drop table d3p1_1;

select count(hashid), 'total' as desc from d2p1_2
union
select count(pattrib), 'wins' as desc from d2p1_2 where presult = 'WIN'
union
select count(pattrib), 'pushes' as desc from d2p1_2 where presult = 'PUSH'
union
select count(pattrib), 'losses' as desc from d2p1_2 where presult = 'LOSE';
