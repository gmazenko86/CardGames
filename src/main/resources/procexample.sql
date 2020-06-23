create or replace procedure addrow_dealerhands(
	hash int, tot int, attrib text, res text,
	c1 text, c2 text, c3 text, c4 text,
	c5 text, c6 text, c7 text, c8 text,
	c9 text, c10 text, c11 text, c12 text
)
as $$
    insert into dealerhands (
		hashid, total, attribute, result, card1, card2, card3, card4,
		card5, card6, card7, card8, card9, card10, card11, card12)
    values (hash, tot, attrib, res, c1, c2, c3, c4,
		   c5, c6, c7, c8, c9, c10, c11, c12)
$$
language sql;

call addrow_dealerhands(
	1, 99, 'BJ99', 'TRIUMPH', 'Ace','Two','Three','Four',
	'Five','Six','Seven','Eight','Nine','Ten','Jack','Queen');