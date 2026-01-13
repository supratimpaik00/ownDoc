alter table patients add column delivery_status varchar(16) default 'pending';
update patients set delivery_status = 'pending' where delivery_status is null;
