--The name structure is <server name>.<database name>.<schema>.<table>.

--SELECT * 
--INTO temp2.dbo.B
--FROM temp.dbo.A

--I get an error:
--ERROR:  cross-database references are not implemented: "codingex.public.dealerhands"
--so I'll just create the table manually

create table dealerhands (
	hashid int primary key,
	total int,
	attribute varchar(12),
	result varchar(12),
	card1 varchar(12),
	card2 varchar(12),
	card3 varchar(12),
	card4 varchar(12),
	card5 varchar(12),
	card6 varchar(12),
	card7 varchar(12),
	card8 varchar(12)	
);

select * into playerhands from dealerhands;